package me.verion.rschem.internal.session;

import lombok.NonNull;
import me.verion.rschem.clipboard.Clipboard;
import me.verion.rschem.internal.clipboard.ClipboardImpl;
import me.verion.rschem.internal.selection.CuboidSelection;
import me.verion.rschem.selection.Selection;
import me.verion.rschem.session.EditSession;
import me.verion.rschem.session.HistoryEntry;
import me.verion.rschem.util.BlockVector3;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Default {@link EditSession} implementation that manages a player's editing state, including their
 * {@link CuboidSelection}, {@link ClipboardImpl}, schematic I/O, and a bounded linear undo/redo history.
 * <p>
 * The session is tied to the owning player by {@link UUID}. The live {@link Player} handle is resolved on demand via
 * {@link Bukkit#getPlayer(UUID)} and is therefore only available while the player is online. Methods that require an
 * online player throw an {@link IllegalStateException} if the player is offline at the time of the call.
 * <p>
 * This implementation is not thread-safe. All method calls must be issued from the server's main thread, except the
 * asynchronous portions of {@link #loadSchematic(Path)} and {@link #saveSchematic(String)}, which explicitly marshal
 * their results back to the main thread before mutating session state.
 *
 * @since 2.0
 */
public final class EditSessionImpl implements EditSession {

  private final UUID uniqueId;
  private final int maxHistorySize;

  private final Deque<HistoryEntry> undoStack = new ArrayDeque<>();
  private final Deque<HistoryEntry> redoStack = new ArrayDeque<>();

  private final CuboidSelection selection = new CuboidSelection();

  private ClipboardImpl clipboard;
  private Path activeSchematic;

  private boolean visualizationEnabled = true;

  /**
   * Constructs a new session for the given player.
   *
   * @param uniqueId       the owning player's UUID; must not be {@code null}
   * @param maxHistorySize the maximum number of undo/redo entries to retain
   */
  public EditSessionImpl(@NonNull UUID uniqueId, int maxHistorySize) {
    this.uniqueId = uniqueId;
    this.maxHistorySize = maxHistorySize;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull UUID uniqueId() {
    return this.uniqueId;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull Optional<Player> player() {
    return Optional.ofNullable(Bukkit.getPlayer(this.uniqueId));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Contract(pure = true)
  public @NonNull Selection selection() {
    return this.selection;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void pos1(@NonNull BlockVector3 pos) {
    this.selection.pos1(this.requiresWorld(), pos);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void pos2(@NonNull BlockVector3 pos) {
    this.selection.pos2(this.requiresWorld(), pos);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Contract(pure = true)
  public @NonNull Optional<Clipboard> clipboard() {
    return Optional.ofNullable(this.clipboard);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void copy() {
    if (!this.selection.complete()) {
      throw new IllegalStateException("Cannot save schematic: selection is incomplete");
    }

    var player = this.requiresPlayer();
    var region = this.selection.toRegion();
    var world = this.selection.world().orElseThrow(() -> new IllegalStateException("selection has no world"));

    var position = BlockVector3.fromLocation(player.getLocation());
    this.clipboard = ClipboardImpl.capture(region, world, position);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void paste(boolean ignoreAir) {
    if (this.clipboard == null) return;

    var player = this.requiresPlayer();
    var world = player.getWorld();
    var pasteOrigin = BlockVector3.fromLocation(player.getLocation()).add(this.clipboard.originOffset());

    var before = new LinkedHashMap<BlockVector3, BlockData>(this.clipboard.blockCount());
    var after = new LinkedHashMap<BlockVector3, BlockData>(this.clipboard.blockCount());

    for (var entry : this.clipboard.blockData().entrySet()) {
      var relative = entry.getKey();
      var pasteData = entry.getValue();

      if (ignoreAir && pasteData.getMaterial().isAir()) continue;

      var position = pasteOrigin.add(relative);
      var block = world.getBlockAt(position.x(), position.y(), position.z());

      before.put(position, block.getBlockData().clone());
      after.put(position, pasteData.clone());

      block.setBlockData(pasteData, false);
    }

    this.recordHistory(world, before, after, "paste");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull Optional<Path> activeSchematic() {
    return Optional.ofNullable(this.activeSchematic);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull CompletableFuture<Void> loadSchematic(@NonNull Path file) {
    return CompletableFuture.failedFuture(new UnsupportedOperationException("not yet implemented"));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull CompletableFuture<Path> saveSchematic(@NonNull String name) {
    if (!this.selection.complete()) {
      throw new IllegalStateException("Cannot save schematic: selection is incomplete");
    }

    // TODO: ADD IMPLEMENTATION
    return CompletableFuture.failedFuture(new UnsupportedOperationException("not yet implemented"));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Contract(pure = true)
  public boolean canUndo() {
    return !this.undoStack.isEmpty();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Contract(pure = true)
  public boolean canRedo() {
    return !this.redoStack.isEmpty();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean undo() {
    if (this.undoStack.isEmpty()) return false;

    var entry = this.undoStack.removeLast();
    restoreState(entry.world(), entry.before());
    this.redoStack.addLast(entry);
    return true;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean redo() {
    if (this.redoStack.isEmpty()) return false;

    var entry = this.redoStack.removeLast();
    this.restoreState(entry.world(), entry.after());
    this.undoStack.addLast(entry);
    return true;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Contract(pure = true)
  public int undoHistorySize() {
    return this.undoStack.size();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Contract(pure = true)
  public boolean isVisualizationEnabled() {
    return this.visualizationEnabled;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setVisualizationEnabled(boolean enabled) {
    this.visualizationEnabled = enabled;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void dispose() {
    this.selection.clear();
    this.clipboard = null;
    this.activeSchematic = null;
    this.undoStack.clear();
    this.redoStack.clear();
  }

  /**
   * Pushes a new {@link HistoryEntry} onto the undo stack and clears the redo stack. If the undo stack exceeds
   * {@link #maxHistorySize} after the push, the oldest entry is evicted from the front of the deque.
   *
   * @param world       the world in which the operation was performed
   * @param before      the pre-operation block states, keyed by absolute world position
   * @param after       the post-operation block states, keyed by absolute world position
   * @param description a short human-readable label for the history entry
   */
  private void recordHistory(
    @NonNull World world,
    @NonNull Map<BlockVector3, BlockData> before,
    @NonNull Map<BlockVector3, BlockData> after,
    @NonNull String description
  ) {
    this.redoStack.clear();
    this.undoStack.addLast(new HistoryEntry(world, before, after, description));

    while (this.undoStack.size() > this.maxHistorySize) {
      this.undoStack.removeFirst();
    }
  }

  /**
   * Restores a set of block states by writing each entry's {@link BlockData} back to the corresponding position in the
   * given world. Block updates are applied without physics ({@code applyPhysics = false}) to avoid cascading side
   * effects during undo/redo operations.
   *
   * @param world the world in which block states are restored
   * @param state a map of absolute world positions to the block data to restore
   */
  private void restoreState(@NonNull World world, @NonNull Map<BlockVector3, BlockData> state) {
    for (var entry : state.entrySet()) {
      var pos = entry.getKey();
      world.getBlockAt(pos.x(), pos.y(), pos.z()).setBlockData(entry.getValue(), false);
    }
  }

  /**
   * Returns the world of the owning player by delegating to {@link #requiresPlayer()}.
   *
   * @return the current world of the owning player
   * @throws IllegalStateException if the owning player is not currently online
   */
  private @NonNull World requiresWorld() {
    return this.requiresPlayer().getWorld();
  }

  /**
   * Returns the owning player, asserting that they are currently online.
   *
   * @return the online {@link Player} associated with this session
   * @throws IllegalStateException if the owning player is not currently online
   */
  private @NonNull Player requiresPlayer() {
    return this.player().orElseThrow(() -> new IllegalStateException("player " + this.uniqueId + " is not online"));
  }
}

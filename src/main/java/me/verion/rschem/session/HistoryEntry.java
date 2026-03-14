package me.verion.rschem.session;

import lombok.NonNull;
import me.verion.rschem.util.BlockVector3;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Map;

/**
 * An immutable snapshot of a single undoable block operation, recording the state of every affected block both before
 * and after the change was applied.
 * <p>
 * Each entry captures the complete {@link BlockData} for every modified position in a given {@link World}, keyed by its
 * {@link BlockVector3} coordinate. The {@code before} map represents the world state prior to the operation, and the
 * {@code after} map the state that was written. Passing these maps to the history system allows the operation to be
 * undone by restoring {@code before}, or redone by reapplying {@code after}.
 * <p>
 * The maps passed to the constructor are stored by reference and are not defensively copied. Callers must ensure that
 * the provided maps are not modified after construction, preferably by wrapping them in
 * {@link java.util.Collections#unmodifiableMap(Map)} or using an immutable map implementation before passing them in.
 *
 * @since 2.0
 */
public final class HistoryEntry {

  private final World world;
  private final Map<BlockVector3, BlockData> before;
  private final Map<BlockVector3, BlockData> after;
  private final String description;

  /**
   * Constructs a new {@link HistoryEntry} for a block operation performed in the given world.
   *
   * @param world       the world in which the operation was performed
   * @param before      a map of every affected block position to its state before the operation
   * @param after       a map of every affected block position to its state after the operation
   * @param description a short human-readable label for this operation, used in history displays
   */
  public HistoryEntry(
    @NonNull World world,
    @NonNull Map<BlockVector3, BlockData> before,
    @NonNull Map<BlockVector3, BlockData> after,
    @NonNull String description
  ) {
    this.world = world;
    this.before = Map.copyOf(before);
    this.after = Map.copyOf(after);
    this.description = description;
  }

  /**
   * Returns the world in which the operation recorded by this entry was performed.
   *
   * @return the world associated with this history entry
   */
  @Contract(pure = true)
  public @NonNull World world() {
    return this.world;
  }

  /**
   * Returns the block state snapshot captured before the operation was applied.
   * <p>
   * Each entry in the map associates a {@link BlockVector3} position with the {@link BlockData} that was present at
   * that position prior to the operation. Restoring these states undoes the operation.
   *
   * @return an unmodified view of the pre-operation block states
   */
  @Contract(pure = true)
  @UnmodifiableView
  public @NonNull Map<BlockVector3, BlockData> before() {
    return this.before;
  }

  /**
   * Returns the block state snapshot captured after the operation was applied.
   * <p>
   * Each entry in the map associates a {@link BlockVector3} position with the {@link BlockData} that was written at
   * that position by the operation. Restoring these states redoes the operation.
   *
   * @return an unmodified view of the post-operation block states
   */
  @Contract(pure = true)
  @UnmodifiableView
  public @NonNull Map<BlockVector3, BlockData> after() {
    return this.after;
  }

  /**
   * Returns a short human-readable label describing the operation recorded by this entry.
   *
   * @return the description of this history entry
   */
  @Contract(pure = true)
  public @NonNull String description() {
    return this.description;
  }

  /**
   * Returns the number of block positions affected by the operation recorded in this entry.
   * <p>
   * The count is derived from the {@link #before()} map, which always contains one entry per affected position
   * regardless of whether the block type actually changed.
   *
   * @return the number of affected block positions, always {@code >= 0}
   */
  @Contract(pure = true)
  public int size() {
    return this.before.size();
  }
}

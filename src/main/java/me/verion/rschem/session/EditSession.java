package me.verion.rschem.session;

import lombok.NonNull;
import me.verion.rschem.clipboard.Clipboard;
import me.verion.rschem.exception.SchematicWriteException;
import me.verion.rschem.selection.Selection;
import me.verion.rschem.util.BlockVector3;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;

import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Represents the complete editing state associated with a single player.
 * <p>
 * An {@code EditSession} bundles all per-player editing concerns into one object: the active {@link Selection}, the
 * {@link Clipboard}, the currently loaded schematic, and a linear undo/redo history. Sessions are created and managed
 * by a {@link SessionManager} and must be disposed of via {@link #dispose()} when no longer needed to release any
 * resources they hold.
 * <p>
 * The session is identified by the {@link UUID} of the owning player and exposes the live {@link Player} handle as an
 * {@link Optional}, which will be empty if the player has gone offline since the session was created.
 * <p>
 * All schematic I/O methods return {@link CompletableFuture} and execute their file-system work off the server's main
 * thread. Callers must not interact with the session's state from the completion callback without returning to the main
 * thread first.
 * <p>
 * This interface is not thread-safe unless stated otherwise on individual methods.
 *
 * @since 2.0
 */
public interface EditSession {

  /**
   * Returns the {@link UUID} of the player that owns this session.
   *
   * @return the owner's unique id
   */
  @Contract(pure = true)
  @NonNull
  UUID uniqueId();

  /**
   * Returns the online {@link Player} handle for the owner of this session, if the player is currently connected.
   *
   * @return an {@link Optional} containing the player, or {@link Optional#empty()} if the player is offline
   */
  @Contract(pure = true)
  @NonNull
  Optional<Player> player();

  /**
   * Returns the {@link Selection} associated with this session. The selection is always present but may be incomplete
   * until both corner positions have been set. Use {@link Selection#complete()} to check readiness.
   *
   * @return the active selection of this session
   */
  @Contract(pure = true)
  @NonNull
  Selection selection();

  /**
   * Sets the first corner of the active selection to the given position.
   *
   * @param pos the block position of the first corner
   */
  void pos1(@NonNull BlockVector3 pos);

  /**
   * Sets the second corner of the active selection to the given position.
   *
   * @param pos the block position of the second corner
   */
  void pos2(@NonNull BlockVector3 pos);

  /**
   * Returns the current clipboard held by this session, if any.
   *
   * @return an {@link Optional} containing the clipboard, or {@link Optional#empty()} if no copy operation has been
   * performed yet
   */
  @Contract(pure = true)
  @NonNull
  Optional<Clipboard> clipboard();

  /**
   * Copies the blocks within the active {@link Selection} into the session's clipboard.
   * <p>
   * The current selection must be complete before calling this method. The copy origin is set to the owning player's
   * current block position, which is stored in the resulting {@link Clipboard} as the {@link Clipboard#originOffset()
   * offset}.
   *
   * @throws IllegalStateException if the active selection is not complete
   */
  void copy();

  /**
   * Pastes the contents of the clipboard into the world at the owning player's current position, applying the stored
   * {@link Clipboard#originOffset() offset}.
   *
   * @param ignoreAir {@code true} to skip air blocks during the paste, preserving the blocks already present at those
   *                  positions; {@code false} to paste all blocks including air
   * @throws IllegalStateException if no clipboard is present in this session
   */
  void paste(boolean ignoreAir);

  /**
   * Returns the path of the schematic file most recently loaded into or saved from this session, if any.
   *
   * @return an {@link Optional} containing the active schematic path, or {@link Optional#empty()} if no schematic
   * operation has been performed
   */
  @Contract(pure = true)
  @NonNull
  Optional<Path> activeSchematic();

  /**
   * Asynchronously loads the schematic at the given path into this session's clipboard.
   *
   * @param file the path to the schematic file to load
   * @return a {@link CompletableFuture} that completes when the schematic has been loaded, or completes exceptionally
   * if the file cannot be read or parsed
   */
  @NonNull
  CompletableFuture<Void> loadSchematic(@NonNull Path file);

  /**
   * Asynchronously saves the current clipboard to a schematic file with the given name.
   *
   * @param name the base file name of the schematic, without a file extension
   * @return a {@link CompletableFuture} that completes with the path of the saved schematic file, or completes
   * exceptionally if the clipboard cannot be serialized or the file cannot be written
   * @throws IllegalStateException   if no clipboard is present in this session
   */
  @NonNull
  CompletableFuture<Path> saveSchematic(@NonNull String name);

  /**
   * Returns whether the undo stack contains at least one entry.
   *
   * @return {@code true} if {@link #undo()} would succeed, {@code false} otherwise
   */
  @Contract(pure = true)
  boolean canUndo();

  /**
   * Returns whether the redo stack contains at least one entry.
   *
   * @return {@code true} if {@link #redo()} would succeed, {@code false} otherwise
   */
  @Contract(pure = true)
  boolean canRedo();

  /**
   * Reverts the most recent undoable operation and moves it onto the redo stack.
   *
   * @return {@code true} if an operation was successfully undone, {@code false} if the undo stack was empty
   */
  boolean undo();

  /**
   * Reapplies the most recently undone operation and moves it back onto the undo stack.
   *
   * @return {@code true} if an operation was successfully redone, {@code false} if the redo stack was empty
   */
  boolean redo();

  /**
   * Returns the number of operations currently on the undo stack.
   *
   * @return the undo history size, always {@code >= 0}
   */
  @Contract(pure = true)
  int undoHistorySize();

  /**
   * Returns whether the selection visualizer is currently enabled for this session.
   *
   * @return {@code true} if the visual overlay is active, {@code false} otherwise
   */
  @Contract(pure = true)
  boolean isVisualizationEnabled();

  /**
   * Enables or disables the selection visualizer for this session.
   *
   * @param enabled {@code true} to enable the visualizer, {@code false} to disable it
   */
  void setVisualizationEnabled(boolean enabled);

  /**
   * Disposes of this session, releasing all resources it holds. This includes clearing the undo and redo stacks,
   * removing any active visual overlay, and discarding the clipboard. After this call the session must not be used
   * further; behavior of any subsequent method calls is undefined.
   */
  void dispose();
}

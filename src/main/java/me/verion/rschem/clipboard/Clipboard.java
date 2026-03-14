package me.verion.rschem.clipboard;

import lombok.NonNull;
import me.verion.rschem.util.BlockRegion;
import me.verion.rschem.util.BlockVector3;
import org.bukkit.World;
import org.jetbrains.annotations.Contract;

/**
 * Represents a captured snapshot of block data that can be pasted into a world.
 * <p>
 * A clipboard is created from a {@link BlockRegion} within a source world and retains both the block data and a
 * reference to the region it was copied from. The {@link #originOffset()} describes the offset between the region's
 * minimum corner and the position that was designated as the copy origin, allowing paste operations to honor the
 * original relative placement of blocks.
 * <p>
 * Clipboard instances are not required to be thread-safe. Concurrent access must be synchronized externally.
 *
 * @since 2.0
 */
public interface Clipboard {

  /**
   * Returns the source region from which this clipboard was captured.
   *
   * @return the {@link BlockRegion} that defines the bounds of the copied area
   */
  @Contract(pure = true)
  @NonNull
  BlockRegion sourceRegion();

  /**
   * Returns the world from which the block data in this clipboard was copied.
   *
   * @return the source {@link World}
   */
  @Contract(pure = true)
  @NonNull
  World sourceWorld();

  /**
   * Returns the offset between the minimum corner of the {@link #sourceRegion()} and the origin point that was used
   * when this clipboard was created.
   * <p>
   * This offset is applied inversely during a paste operation to restore blocks at their correct position relative
   * to the chosen paste target.
   *
   * @return the origin offset as a {@link BlockVector3}
   */
  @Contract(pure = true)
  @NonNull
  BlockVector3 originOffset();

  /**
   * Returns whether this clipboard contains no block data.
   *
   * @return {@code true} if no blocks have been captured, {@code false} otherwise
   */
  @Contract(pure = true)
  boolean empty();

  /**
   * Returns the number of blocks stored in this clipboard.
   *
   * @return the number of blocks held by this clipboard, always {@code >= 0}
   */
  @Contract(pure = true)
  int blockCount();
}

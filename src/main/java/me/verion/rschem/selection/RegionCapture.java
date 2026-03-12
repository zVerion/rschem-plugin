package me.verion.rschem.selection;

import com.google.common.base.Preconditions;
import lombok.NonNull;
import me.verion.rschem.model.BlockPalette;
import me.verion.rschem.model.RoomDimensions;
import org.bukkit.util.BlockVector;
import org.jetbrains.annotations.Contract;

/**
 * An immutable snapshot of all block states within a {@link BlockSelection}, captured synchronously on the server's
 * main thread.
 *
 * <p>Block states are stored in a palette-compressed format: a {@link BlockPalette} maps every distinct block-state
 * string to a compact integer index, and {@code blockData} holds one index per block position.
 *
 * <pre>{@code
 * RegionCapture snapshot = RegionCapture.capture(selection);
 * int index = snapshot.blockData()[snapshot.dimensions().blockIndex(x, y, z)];
 * String state = snapshot.palette().getById(index);
 * }</pre>
 *
 * @param palette    the block-state palette used to encode {@code blockData}, never {@code null}.
 * @param blockData  palette-index array, one entry per block position,  never {@code null}.
 * @param dimensions the spatial dimensions describing the captured region, never {@code null}.
 * @since 1.0
 */
public record RegionCapture(@NonNull BlockPalette palette, int[] blockData, @NonNull RoomDimensions dimensions) {

  /**
   * Captures all block states within {@code selection} and returns an immutable {@code RegionCapture} snapshot.
   *
   * <p><strong>Thread safety:</strong> this method reads directly from the Bukkit world and <em>must</em> be called on
   * the server's main thread.
   *
   * <p>Block iteration order is {@code y → z → x} (Y-major), matching the index layout of
   * {@link RoomDimensions#blockIndex(int, int, int)}.
   *
   * @param selection the region to capture, never {@code null}.
   * @return a new, immutable {@code RegionCapture}, never {@code null}.
   * @throws NullPointerException if {@code selection} is {@code null}.
   */
  @Contract("_ -> new")
  public static @NonNull RegionCapture capture(@NonNull BlockSelection selection) {
    Preconditions.checkNotNull(selection, "selection must not be null");

    int width = selection.width();
    int height = selection.height();
    int depth = selection.depth();

    RoomDimensions dimensions = new RoomDimensions(width, height, depth);
    BlockPalette palette = new BlockPalette();
    int[] blockData = new int[dimensions.volume()];

    BlockVector min = selection.min();

    for (int y = 0; y < height; y++) {
      for (int z = 0; z < depth; z++) {
        for (int x = 0; x < width; x++) {
          var block = selection.world().getBlockAt(
            min.getBlockX() + x,
            min.getBlockY() + y,
            min.getBlockZ() + z);
          int index = palette.getOrAdd(block.getBlockData().getAsString());

          blockData[dimensions.blockIndex(x, y, z)] = index;
        }
      }
    }
    return new RegionCapture(palette, blockData, dimensions);
  }
}

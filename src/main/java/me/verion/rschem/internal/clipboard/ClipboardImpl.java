package me.verion.rschem.internal.clipboard;

import lombok.NonNull;
import me.verion.rschem.clipboard.Clipboard;
import me.verion.rschem.util.BlockRegion;
import me.verion.rschem.util.BlockVector3;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.HashMap;
import java.util.Map;

/**
 * Default {@link Clipboard} implementation that stores copied block data as an immutable position-to-{@link BlockData}
 * map with coordinates relative to the source region's minimum corner.
 * <p>
 * Instances are created either directly via the constructor — when the caller already holds a prepared block-data map
 * — or through the {@link #capture(BlockRegion, World, BlockVector3)} factory method, which reads block data live from
 * the world and computes the origin offset automatically from the capturing player's position.
 * <p>
 * All block data is stored defensively: the map passed to the constructor is copied via {@link Map#copyOf(Map)}, and
 * each {@link BlockData} value captured by {@link #capture(BlockRegion, World, BlockVector3)} is cloned at capture time.
 * Subsequent changes to the source world or the original map therefore do not affect this clipboard.
 *
 * @since 2.0
 */
public final class ClipboardImpl implements Clipboard {

  private final BlockRegion sourceRegion;
  private final World sourceWorld;
  private final BlockVector3 originOffset;
  private final Map<BlockVector3, BlockData> blockData;

  /**
   * Constructs a new {@code ClipboardImpl} from a pre-built block-data map.
   *
   * @param sourceRegion the region from which the block data was copied
   * @param sourceWorld  the world from which the block data was read
   * @param originOffset the offset from the region's minimum corner to the capture origin
   * @param blockData    a map of region-relative positions to their corresponding block data
   */
  public ClipboardImpl(
    @NonNull BlockRegion sourceRegion,
    @NonNull World sourceWorld,
    @NonNull BlockVector3 originOffset,
    @NonNull Map<BlockVector3, BlockData> blockData
  ) {
    this.sourceRegion = sourceRegion;
    this.sourceWorld = sourceWorld;
    this.originOffset = originOffset;
    this.blockData = Map.copyOf(blockData);
  }

  /**
   * Captures the block data of every position in the given region from the given world and returns a new
   * {@code ClipboardImpl} containing the snapshot.
   * <p>
   * Coordinates in the resulting clipboard are stored relative to the minimum corner of {@code region}, so that the
   * clipboard remains independent of its original world position. The {@link #originOffset()} is computed as
   * {@code region.min().subtract(playerPosition)}, which allows a subsequent paste operation to place blocks at the
   * correct position relative to the pasting player.
   * <p>
   * Each {@link BlockData} value is cloned at capture time. Subsequent world changes do not affect the returned
   * clipboard.
   * <p>
   * For very large regions this method may perform a significant number of world reads and should be called
   * asynchronously where possible.
   *
   * @param region         the region whose blocks are captured
   * @param world          the world from which block data is read
   * @param playerPosition the block position of the player performing the copy, used to
   *                       compute the origin offset
   * @return a new {@code ClipboardImpl} containing the captured block data
   */
  @Contract("_, _, _ -> new")
  public static @NonNull ClipboardImpl capture(
    @NonNull BlockRegion region,
    @NonNull World world,
    @NonNull BlockVector3 playerPosition
  ) {
    var data = new HashMap<BlockVector3, BlockData>((int) Math.min(region.volume(), Integer.MAX_VALUE));

    for (var position : region) {
      var blockData = world.getBlockAt(position.x(), position.y(), position.z()).getBlockData();
      var relative = position.subtract(region.min());
      data.put(relative, blockData.clone());
    }

    var offset = region.min().subtract(playerPosition);
    return new ClipboardImpl(region, world, offset, data);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Contract(pure = true)
  public @NonNull BlockRegion sourceRegion() {
    return this.sourceRegion;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Contract(pure = true)
  public @NonNull World sourceWorld() {
    return this.sourceWorld;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Contract(pure = true)
  public @NonNull BlockVector3 originOffset() {
    return this.originOffset;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Contract(pure = true)
  public boolean empty() {
    return this.blockData.isEmpty();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Contract(pure = true)
  public int blockCount() {
    return this.blockData.size();
  }

  /**
   * Returns an unmodifiable view of the captured block data.
   *
   * @return an unmodifiable map of region-relative positions to their block data
   */
  @Contract(pure = true)
  @UnmodifiableView
  public @NonNull Map<BlockVector3, BlockData> blockData() {
    return this.blockData;
  }
}

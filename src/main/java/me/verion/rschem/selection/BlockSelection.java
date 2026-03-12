package me.verion.rschem.selection;

import com.google.common.base.Preconditions;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.BlockVector;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

/**
 * Represents an immutable, axis-aligned cuboid selection within a {@link World}, defined by two corner positions. The
 * selection is automatically normalized so that {@link #min()} always holds the smallest and {@link #max()} the largest
 * coordinate on every axis.
 *
 * <pre>{@code
 * var selection = BlockSelection.builder()
 *    .world(world)
 *    .pos1(new BlockVector(0, 64, 0))
 *    .pos2(new BlockVector(15, 80, 15))
 *    .build();
 *
 * int volume = selection.volume();
 * }</pre>
 *
 * @since 1.0
 */
public final class BlockSelection {

  private final World world;
  private final BlockVector pos1;
  private final BlockVector pos2;

  private final BlockVector min;
  private final BlockVector max;

  /**
   * Constructs a new {@code BlockSelection} with the given world and corner positions. The two corners are normalized
   * internally so that {@link #min()} and {@link #max()} always represent the true lower and upper bounds regardless of
   * the order in which {@code pos1} and {@code pos2} were supplied.
   *
   * @param world the world in which the selection resides, never {@code null}
   * @param pos1  first corner of the selection, never {@code null}
   * @param pos2  second corner of the selection, never {@code null}
   * @throws NullPointerException if any argument is {@code null}.
   */
  private BlockSelection(@NonNull World world, @NonNull BlockVector pos1, @NonNull BlockVector pos2) {
    this.world = world;
    this.pos1 = pos1;
    this.pos2 = pos2;

    this.min = new BlockVector(
      Math.min(pos1.getBlockX(), pos2.getBlockX()),
      Math.min(pos1.getBlockY(), pos2.getBlockY()),
      Math.min(pos1.getBlockZ(), pos2.getBlockZ())
    );

    this.max = new BlockVector(
      Math.max(pos1.getBlockX(), pos2.getBlockX()),
      Math.max(pos1.getBlockY(), pos2.getBlockY()),
      Math.max(pos1.getBlockZ(), pos2.getBlockZ())
    );
  }

  /**
   * Creates a new {@code BlockSelection} from two arbitrary corner vectors. The vectors do not need to be pre-sorted;
   * normalization is applied internally.
   *
   * @param world the world in which the selection resides, never {@code null}.
   * @param pos1  first corner of the selection, never {@code null}.
   * @param pos2  second corner of the selection, never {@code null}.
   * @return a new, immutable {@code BlockSelection}.
   * @throws NullPointerException if any argument is {@code null}.
   */
  @Contract("_, _, _ -> new")
  public static @NonNull BlockSelection of(@NonNull World world, @NonNull BlockVector pos1, @NonNull BlockVector pos2) {
    return builder()
      .world(world)
      .pos1(pos1)
      .pos2(pos2)
      .build();
  }

  /**
   * Creates a new block selection builder instance.
   *
   * @return the new builder instance.
   */
  @Contract("-> new")
  public static @NotNull Builder builder() {
    return new Builder();
  }

  /**
   * Creates a new block selection builder instance and copies all values of the given selection into the new builder.
   *
   * @param selection the block selection to copy from.
   * @return the new builder instance with values of the given permission.
   * @throws NullPointerException if the given permission is null.
   */
  @Contract("_, -> new")
  public static @NonNull Builder builder(@NonNull BlockSelection selection) {
    return builder()
      .world(selection.world())
      .pos1(selection.pos1())
      .pos2(selection.pos2());
  }

  /**
   * Returns the world in which this selection is located.
   *
   * @return the world, never {@code null}.
   */
  public @NotNull World world() {
    return this.world;
  }

  /**
   * Returns the first corner as supplied by the creator of this selection. Use {@link #min()} / {@link #max()} for
   * normalized, axis-safe access.
   *
   * @return pos1, never {@code null}.
   */
  public @NotNull BlockVector pos1() {
    return this.pos1;
  }

  /**
   * Returns the second corner as supplied by the creator of this selection. Use {@link #min()} / {@link #max()} for
   * normalized, axis-safe access.
   *
   * @return pos2, never {@code null}.
   */
  public @NotNull BlockVector pos2() {
    return this.pos2;
  }

  /**
   * Returns the corner with the smallest X, Y and Z values (lower bound).
   *
   * @return the normalized minimum corner, never {@code null}.
   */
  public @NotNull BlockVector min() {
    return this.min;
  }

  /**
   * Returns the corner with the largest X, Y and Z values (upper bound).
   *
   * @return the normalized maximum corner, never {@code null}.
   */
  public @NotNull BlockVector max() {
    return this.max;
  }

  /**
   * Returns the width of this selection, measured along the X-axis. The value is always {@code ≥ 1}.
   *
   * @return width in blocks.
   */
  @Range(from = 1, to = Integer.MAX_VALUE)
  public int width() {
    return this.max.getBlockX() - this.min.getBlockX() + 1;
  }

  /**
   * Returns the height of this selection, measured along the Y-axis. The value is always {@code ≥ 1}.
   *
   * @return height in blocks.
   */
  @Range(from = 1, to = Integer.MAX_VALUE)
  public int height() {
    return this.max.getBlockY() - this.min.getBlockY() + 1;
  }

  /**
   * Returns the depth of this selection, measured along the Z-axis. The value is always {@code ≥ 1}.
   *
   * @return depth in blocks.
   */
  @Range(from = 1, to = Integer.MAX_VALUE)
  public int depth() {
    return this.max.getBlockZ() - this.min.getBlockZ() + 1;
  }

  /**
   * Returns the total block volume of this selection ({@code width × height × depth}).
   *
   * @return volume in blocks, always {@code ≥ 1}.
   */
  @Range(from = 1, to = Integer.MAX_VALUE)
  public int volume() {
    return width() * height() * depth();
  }

  /**
   * Returns whether a given {@link BlockVector} is contained within this selection (inclusive on all sides).
   *
   * @param vector the vector to test, never {@code null}.
   * @return {@code true} if the vector lies inside this selection.
   */
  public boolean contains(@NotNull BlockVector vector) {
    Preconditions.checkNotNull(vector, "vector must not be null");
    return vector.getBlockX() >= this.min.getBlockX() && vector.getBlockX() <= this.max.getBlockX()
      && vector.getBlockY() >= this.min.getBlockY() && vector.getBlockY() <= this.max.getBlockY()
      && vector.getBlockZ() >= this.min.getBlockZ() && vector.getBlockZ() <= this.max.getBlockZ();
  }

  /**
   * Returns whether this selection overlaps with another one inside the same world. Two selections are considered
   * overlapping if their cuboid volumes share at least one block.
   *
   * @param other the other selection to test against, never {@code null}.
   * @return {@code true} if the selections intersect, {@code false} if they are in different worlds or do not touch.
   */
  public boolean overlaps(@NonNull BlockSelection other) {
    Preconditions.checkNotNull(other, "other must not be null");
    if (!this.world.equals(other.world)) return false;
    return this.min.getBlockX() <= other.max.getBlockX() && this.max.getBlockX() >= other.min.getBlockX()
      && this.min.getBlockY() <= other.max.getBlockY() && this.max.getBlockY() >= other.min.getBlockY()
      && this.min.getBlockZ() <= other.max.getBlockZ() && this.max.getBlockZ() >= other.min.getBlockZ();
  }

  /**
   * Returns a world-space {@link Location} anchored at the {@link #min()} corner of this selection. Useful as an origin
   * for paste or copy operations.
   *
   * @return the minimum corner as a {@link Location}, never {@code null}.
   */
  @Contract("-> new")
  public @NotNull Location minLocation() {
    return new Location(this.world, this.min.getBlockX(), this.min.getBlockY(), this.min.getBlockZ());
  }

  /**
   * Returns a world-space {@link Location} anchored at the {@link #max()} corner of this selection.
   *
   * @return the maximum corner as a {@link Location}, never {@code null}.
   */
  @Contract("-> new")
  public @NotNull Location maxLocation() {
    return new Location(this.world, this.max.getBlockX(), this.max.getBlockY(), this.max.getBlockZ());
  }

  /**
   * A builder for a BlockSelection.
   *
   * @since 1.0
   */
  public static final class Builder {

    private World world;
    private BlockVector pos1;
    private BlockVector pos2;

    /**
     * Sets the world for the selection.
     *
     * @param world the world, never {@code null}.
     * @return the same instance as used to call the method, for chaining.
     */
    @Contract("_ -> this")
    public @NonNull Builder world(@NonNull World world) {
      this.world = world;
      return this;
    }

    /**
     * Sets the first corner of the selection.
     *
     * @param pos1 the first corner, never {@code null}.
     * @return the same instance as used to call the method, for chaining.
     */
    @Contract("_ -> this")
    public @NonNull Builder pos1(@NonNull BlockVector pos1) {
      this.pos1 = pos1;
      return this;
    }

    /**
     * Sets the second corner of the selection.
     *
     * @param pos2 the second corner, never {@code null}.
     * @return the same instance as used to call the method, for chaining.
     */
    @Contract("_ -> this")
    public @NonNull Builder pos2(@NonNull BlockVector pos2) {
      this.pos2 = pos2;
      return this;
    }

    /**
     * Builds the new block selection with all previously set options.
     *
     * @return the new selection, never {@code null}.
     * @throws IllegalStateException if {@code world}, {@code pos1} or {@code pos2} have not been set.
     */
    @Contract("-> new")
    public @NotNull BlockSelection build() {
      Preconditions.checkState(this.world != null, "No world given");
      Preconditions.checkState(this.pos1 != null, "No pos1 given");
      Preconditions.checkState(this.pos2 != null, "No pos2 given");

      return new BlockSelection(this.world, this.pos1, this.pos2);
    }
  }
}

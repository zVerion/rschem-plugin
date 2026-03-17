package me.verion.rschem.api.util;

import lombok.NonNull;
import org.jetbrains.annotations.Contract;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An immutable, axis-aligned bounding box defined by two {@link BlockVector3} corners in integer block coordinates.
 * <p>
 * The region is always stored in a normalized form: {@link #min} is guaranteed to hold the component-wise minimum and
 * {@link #max} the component-wise maximum of the two input positions. Use the factory method
 * {@link #of(BlockVector3, BlockVector3)} to construct instances from arbitrary corner pairs; direct record
 * construction should only be used when the corners are already known to be normalized.
 * <p>
 * Iteration over all block positions contained in the region is supported via {@link Iterable}. The traversal order is
 * X&nbsp;(outer) → Z&nbsp;(middle) → Y&nbsp;(inner), which matches the column-major memory layout of Minecraft chunks
 * and therefore tends to be cache-friendly when reading or writing block data.
 *
 * @param min the component-wise minimum corner of this region
 * @param max the component-wise maximum corner of this region
 * @since 2.0
 */
public record BlockRegion(@NonNull BlockVector3 min, @NonNull BlockVector3 max) implements Iterable<BlockVector3> {

  /**
   * Creates a normalised {@link BlockRegion} spanning the two given positions.
   * <p>
   * The two positions do not need to be ordered; the factory derives the correct {@link #min} and {@link #max} corners
   * by taking the component-wise minimum and maximum.
   *
   * @param pos1 the first corner position
   * @param pos2 the second corner position
   * @return a normalised {@link BlockRegion} enclosing both positions
   */
  @Contract(value = "_, _ -> new", pure = true)
  public static @NonNull BlockRegion of(@NonNull BlockVector3 pos1, @NonNull BlockVector3 pos2) {
    return new BlockRegion(pos1.min(pos2), pos1.max(pos2));
  }

  /**
   * Returns the number of blocks this region spans along the x-axis.
   *
   * @return the extent of this region on the x-axis, always {@code >= 1}
   */
  @Contract(pure = true)
  public int sizeX() {
    return this.max.x() - this.min.x() + 1;
  }

  /**
   * Returns the number of blocks this region spans along the y-axis.
   *
   * @return the extent of this region on the y-axis, always {@code >= 1}
   */
  @Contract(pure = true)
  public int sizeY() {
    return this.max.y() - this.min.y() + 1;
  }

  /**
   * Returns the number of blocks this region spans along the z-axis.
   *
   * @return the extent of this region on the z-axis, always {@code >= 1}
   */
  @Contract(pure = true)
  public int sizeZ() {
    return this.max.z() - this.min.z() + 1;
  }

  /**
   * Returns the total number of block positions enclosed by this region.
   * <p>
   * The result is computed as {@code sizeX * sizeY * sizeZ} and is cast to {@code long} to avoid integer overflow for
   * large regions.
   *
   * @return the volume of this region in number of blocks, always {@code >= 1}
   */
  @Contract(pure = true)
  public long volume() {
    return (long) sizeX() * sizeY() * sizeZ();
  }

  /**
   * Returns the geometric center of this region as a floating-point coordinate triple.
   * <p>
   * The returned array contains exactly three elements in the order {@code [centerX, centerY, centerZ]}.
   *
   * @return a three-element {@code double} array representing the center of this region
   */
  @Contract(value = "-> new", pure = true)
  public double @NonNull [] center() {
    return new double[]{
      (this.min.x() + this.max.x()) / 2.0,
      (this.min.y() + this.max.y()) / 2.0,
      (this.min.z() + this.max.z()) / 2.0
    };
  }

  /**
   * Returns whether the given block position is inside this region (inclusive on all sides).
   *
   * @param pos the position to test
   * @return {@code true} if {@code pos} lies within the bounds of this region, {@code false} otherwise
   */
  @Contract(value = "_ -> _", pure = true)
  public boolean contains(@NonNull BlockVector3 pos) {
    return pos.x() >= this.min.x() && pos.x() <= this.max.x()
      && pos.y() >= this.min.y() && pos.y() <= this.max.y()
      && pos.z() >= this.min.z() && pos.z() <= this.max.z();
  }

  /**
   * Returns a lazy iterator over every {@link BlockVector3} contained in this region.
   * <p>
   * Positions are yielded in X&nbsp;(outer) → Z&nbsp;(middle) → Y&nbsp;(inner) order. The iterator does not support
   * removal.
   *
   * @return an iterator over all block positions in this region
   */
  @Override
  @Contract(value = "-> new", pure = true)
  public @NonNull Iterator<BlockVector3> iterator() {
    return new RegionIterator();
  }

  /**
   * A lazy, stateful iterator over all block positions contained in the enclosing {@link BlockRegion}.
   * <p>
   * Positions are yielded in X&nbsp;(outer) → Z&nbsp;(middle) → Y&nbsp;(inner) order, matching the column-major layout
   * of Minecraft chunks for improved cache locality when performing bulk block operations.
   * <p>
   * This iterator does not support the optional {@link Iterator#remove()} operation.
   *
   * @since 2.0
   */
  private final class RegionIterator implements Iterator<BlockVector3> {

    private int cx = min.x();
    private int cy = min.y();
    private int cz = min.z();
    private boolean hasNext = volume() > 0;

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasNext() {
      return this.hasNext;
    }

    /**
     * Returns the next block position in iteration order and advances the cursor.
     *
     * @return the next {@link BlockVector3} in this region
     * @throws NoSuchElementException if the iteration has no more elements
     */
    @Override
    public @NonNull BlockVector3 next() {
      if (!this.hasNext) {
        throw new NoSuchElementException();
      }

      var current = BlockVector3.of(this.cx, this.cy, this.cz);
      this.advance();
      return current;
    }

    /**
     * Advances the internal cursor to the next position in X → Z → Y order, or marks the iteration as finished when the
     * last position has been consumed.
     */
    private void advance() {
      if (++this.cy > max.y()) {
        this.cy = min.y();

        if (++this.cz > max.z()) {
          this.cz = min.z();

          if (++this.cx > max.x()) {
            this.hasNext = false;
          }
        }
      }
    }
  }
}

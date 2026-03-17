package me.verion.rschem.api.util;

import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.BlockVector;
import org.jetbrains.annotations.Contract;

/**
 * An immutable, three-dimensional vector of integer block coordinates.
 * <p>
 * This record represents a position or offset in a block-based world using integer precision, as opposed to
 * floating-point vectors. Common use cases include referencing block positions, computing bounding boxes, or expressing
 * directional offsets within a world grid.
 * <p>
 * Cached constants for the zero vector and the three unit-axis vectors are provided as static fields. The factory
 * method {@link #of(int, int, int)} returns the shared {@link #ZERO} constant when all components are zero, avoiding
 * unnecessary allocations.
 *
 * @param x the x-component of this vector
 * @param y the y-component of this vector
 * @param z the z-component of this vector
 * @since 2.0
 */
public record BlockVector3(int x, int y, int z) {

  // a shard constant representing the zero vector {@code (0, 0, 0)}
  public static final BlockVector3 ZERO = new BlockVector3(0, 0, 0);

  // a shared constant representing the positive x-axis unit vector {@code (1, 0, 0)}
  public static final BlockVector3 UNIT_X = new BlockVector3(1, 0, 0);

  // a shared constant representing the positive y-axis unit vector {@code (0, 1, 0)}
  public static final BlockVector3 UNIT_Y = new BlockVector3(0, 1, 0);

  // a shared constant representing the positive z-axis unit vector {@code (0, 0, 1)}
  public static final BlockVector3 UNIT_Z = new BlockVector3(0, 0, 1);

  /**
   * Returns a {@link BlockVector3} with the given components, reusing {@link #ZERO} when all components are zero.
   *
   * @param x the x-component
   * @param y the y-component
   * @param z the z-component
   * @return a {@link BlockVector3} representing the given coordinates
   */
  @Contract(value = "_, _, _ -> new", pure = true)
  public static @NonNull BlockVector3 of(int x, int y, int z) {
    if (x == 0 && y == 0 && z == 0) return ZERO;
    return new BlockVector3(x, y, z);
  }

  /**
   * Creates a {@link BlockVector3} from the block coordinates of the given location.
   *
   * @param location the location whose block coordinates are used
   * @return a {@link BlockVector3} at the block position of the given location
   */
  @Contract(value = "_ -> new", pure = true)
  public static @NonNull BlockVector3 fromLocation(@NonNull Location location) {
    return of(location.getBlockX(), location.getBlockY(), location.getBlockZ());
  }

  /**
   * Returns a new vector that is the component-wise sum of this vector and {@link BlockVector3 other}.
   *
   * @param other the vector to add
   * @return a new {@link BlockVector3} representing the sum
   */
  @Contract(value = "_ -> new", pure = true)
  public @NonNull BlockVector3 add(@NonNull BlockVector3 other) {
    return of(this.x + other.x(), this.y + other.y(), this.z + other.z());
  }

  /**
   * Returns a new vector that is the component-wise sum of this vector and the given offsets.
   *
   * @param dx the offset to add to the x-component
   * @param dy the offset to add to the y-component
   * @param dz the offset to add to the z-component
   * @return a new {@code BlockVector3} representing the sum
   */
  @Contract(value = "_, _, _ -> new", pure = true)
  public @NonNull BlockVector3 add(int dx, int dy, int dz) {
    return of(this.x + dx, this.y + dy, this.z + dz);
  }

  /**
   * Returns a new vector that is the component-wise difference of this vector and {@link BlockVector3 other}.
   *
   * @param other the vector to subtract
   * @return a new {@link BlockVector3} representing the difference
   */
  @Contract(value = "_ -> new", pure = true)
  public @NonNull BlockVector3 subtract(@NonNull BlockVector3 other) {
    return of(this.x - other.x(), this.y - other.y(), this.z - other.z());
  }

  /**
   * Returns a new vector whose components are the component-wise minimum of this vector and {@link BlockVector3 other}.
   *
   * @param other the vector to compare against
   * @return a new {@link BlockVector3} with the minimum components of both vectors
   */
  @Contract(value = "_ -> new", pure = true)
  public @NonNull BlockVector3 min(@NonNull BlockVector3 other) {
    return of(Math.min(this.x, other.x()), Math.min(this.y, other.y()), Math.min(this.z, other.z()));
  }

  /**
   * Returns a new vector whose components are the component-wise maximum of this vector and {@link BlockVector3 other}.
   *
   * @param other the vector to compare against
   * @return a new {@link BlockVector3} with the maximum components of both vectors
   */
  @Contract(value = "_ -> new", pure = true)
  public @NonNull BlockVector3 max(@NonNull BlockVector3 other) {
    return of(Math.max(this.x, other.x()), Math.max(this.y, other.y()), Math.max(this.z, other.z()));
  }

  /**
   * Converts this vector to a {@link Location} in the given world, using the vector's components as the x, y, and z
   * coordinates.
   *
   * @param world the world in which the location is created
   * @return a {@link Location} representing this vector's position in the given world
   */
  public @NonNull Location toLocation(@NonNull World world) {
    return new Location(world, this.x, this.y, this.z);
  }

  /**
   * Converts this vector to a Bukkit {@link BlockVector}.
   *
   * @return a Bukkit {@link BlockVector} with the same coordinates
   */
  @Contract(value = "-> new", pure = true)
  public @NonNull BlockVector toBlockVector() {
    return new BlockVector(this.x, this.y, this.z);
  }

  /**
   * Returns the squared Euclidean distance between this vector and {@link BlockVector3 other}.
   * <p>
   * This method avoids the cost of a square-root computation and is therefore preferred for distance comparisons where
   * only relative ordering matters.
   *
   * @param other the vector to measure the distance to
   * @return the squared distance between the two vectors as a long
   */
  public long distanceToSquared(@NonNull BlockVector3 other) {
    long dx = this.x - other.x();
    long dy = this.y - other.y();
    long dz = this.z - other.z();
    return dx * dx + dy * dy + dz * dz;
  }

  /**
   * Returns the Euclidean distance between this vector and {@link BlockVector3 other}.
   * <p>
   * Where only a distance comparison is required, prefer {@link #distanceToSquared(BlockVector3)} to avoid the overhead
   * of the square-root operation.
   *
   * @param other the vector to measure the distance to
   * @return the Euclidean distance between the two vectors
   */
  public double distanceTo(@NonNull BlockVector3 other) {
    return Math.sqrt(this.distanceToSquared(other));
  }
}

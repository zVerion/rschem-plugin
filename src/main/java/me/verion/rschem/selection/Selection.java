package me.verion.rschem.selection;

import lombok.NonNull;
import me.verion.rschem.util.BlockRegion;
import me.verion.rschem.util.BlockVector3;
import org.bukkit.World;
import org.jetbrains.annotations.Contract;

import java.util.Optional;

/**
 * Represents a mutable, two-point block selection tied to a specific world.
 * <p>
 * A selection is built incrementally by setting two corner positions via {@link #pos1(World, BlockVector3)} and
 * {@link #pos2(World, BlockVector3)}. Until both positions and a world have been assigned, the selection is considered
 * incomplete and cannot be converted to a {@link BlockRegion}. Use {@link #complete()} to check readiness before
 * calling {@link #toRegion()}.
 * <p>
 * All mutable state may be reset at any time through {@link #clear()}, returning the selection to its initial, empty
 * state.
 *
 * @since 2.0
 */
public interface Selection {

  /**
   * Returns the world this selection is associated with, if one has been set.
   *
   * @return an {@link Optional} containing the world, or {@link Optional#empty()} if no position has been set yet
   */
  @Contract(pure = true)
  Optional<World> world();

  /**
   * Returns the first corner position of this selection, if one has been set.
   *
   * @return an {@link Optional} containing the first corner, or {@link Optional#empty()} if
   * {@link #pos1(World, BlockVector3)} has not yet been called
   */
  @Contract(pure = true)
  Optional<BlockVector3> pos1();

  /**
   * Returns the second corner position of this selection, if one has been set.
   *
   * @return an {@link Optional} containing the second corner, or {@link Optional#empty()} if
   * {@link #pos2(World, BlockVector3)} has not yet been called
   */
  @Contract(pure = true)
  Optional<BlockVector3> pos2();

  /**
   * Sets the first corner position of this selection and associates it with the given world.
   *
   * @param world    the world in which the position resides
   * @param position the block position of the first corner
   */
  void pos1(@NonNull World world, @NonNull BlockVector3 position);

  /**
   * Sets the second corner position of this selection and associates it with the given world.
   *
   * @param world    the world in which the position resides
   * @param position the block position of the second corner
   */
  void pos2(@NonNull World world, @NonNull BlockVector3 position);

  /**
   * Returns whether this selection is fully defined and ready to be converted to a region.
   * <p>
   * A selection is considered complete when both corner positions and a world have been assigned. Only complete
   * selections may be passed to {@link #toRegion()}.
   *
   * @return {@code true} if both corners and a world are set, {@code false} otherwise
   */
  @Contract(pure = true)
  boolean complete();

  /**
   * Converts this selection to an immutable {@link BlockRegion}.
   * <p>
   * This method must only be called when {@link #complete()} returns {@code true}. Implementations are free to throw an
   * exception to their choice when invoked on an incomplete selection.
   *
   * @return a {@link BlockRegion} spanning the two corner positions of this selection
   * @throws IllegalStateException if this selection is not yet complete
   */
  @Contract(pure = true)
  @NonNull
  BlockRegion toRegion();

  /**
   * Resets this selection, clearing all stored positions and the associated world.
   * <p>
   * After this call, {@link #complete()} ()} will return {@code false} and both {@link #pos1()} and {@link #pos2()}
   * will return {@link Optional#empty()}.
   */
  void clear();

  /**
   * Returns the volume of this selection in number of block positions, or {@code 0} if the selection is not yet
   * complete.
   * <p>
   * This is a convenience shorthand for {@code isComplete() ? toRegion().volume() : 0}.
   *
   * @return the volume of the underlying region, or {@code 0L} if the selection is incomplete
   */
  @Contract(pure = true)
  default long volume() {
    return this.complete() ? this.toRegion().volume() : 0L;
  }
}

package me.verion.rschem.internal.selection;

import lombok.NonNull;
import me.verion.rschem.selection.Selection;
import me.verion.rschem.util.BlockRegion;
import me.verion.rschem.util.BlockVector3;
import org.bukkit.World;

import java.util.Optional;

/**
 * A mutable, cuboid {@link Selection} implementation backed by two {@link BlockVector3} corner positions within a
 * single {@link World}.
 * <p>
 * State is accumulated incrementally: each call to {@link #pos1(World, BlockVector3)} or
 * {@link #pos2(World, BlockVector3)} overwrites the respective corner and updates the associated world. The selection
 * is considered complete — and safe to convert via {@link #toRegion()} — once both corners have been set.
 * <p>
 * This implementation is not thread-safe. External synchronization is required if the selection is accessed from
 * multiple threads.
 *
 * @since 2.0
 */
public class CuboidSelection implements Selection {

  private World world;
  private BlockVector3 pos1;
  private BlockVector3 pos2;

  /**
   * {@inheritDoc}
   */
  @Override
  public Optional<World> world() {
    return Optional.ofNullable(this.world);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Optional<BlockVector3> pos1() {
    return Optional.ofNullable(this.pos1);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Optional<BlockVector3> pos2() {
    return Optional.ofNullable(this.pos2);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void pos1(@NonNull World world, @NonNull BlockVector3 position) {
    if (this.pos2 != null && this.world != null && !this.world.equals(world)) {
      this.pos2 = null;
    }
    this.world = world;
    this.pos1 = position;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void pos2(@NonNull World world, @NonNull BlockVector3 position) {
    if (this.pos1 != null && this.world != null && !this.world.equals(world)) {
      this.pos1 = null;
    }
    this.world = world;
    this.pos2 = position;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean complete() {
    return this.pos1 != null && this.pos2 != null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull BlockRegion toRegion() {
    if (!this.complete()) {
      throw new IllegalStateException("Selection is incomplete; both positions must be set before calling toRegion()");
    }
    return BlockRegion.of(this.pos1, this.pos2);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void clear() {
    this.pos1 = null;
    this.pos2 = null;
    this.world = null;
  }
}

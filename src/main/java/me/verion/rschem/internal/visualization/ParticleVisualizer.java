package me.verion.rschem.internal.visualization;

import lombok.NonNull;
import me.verion.rschem.selection.Selection;
import me.verion.rschem.util.BlockRegion;
import me.verion.rschem.util.BlockVector3;
import me.verion.rschem.visualization.SelectionVisualizer;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A {@link SelectionVisualizer} implementation that draws the bounding box of a {@link Selection} using Minecraft's
 * {@link Particle#DUST} effect.
 * <p>
 * The twelve edges of the bounding box are rendered as evenly-spaced particle chains, and all eight corners are
 * highlighted with larger dust particles. Both edge and corner colors are configurable at construction time. The edge
 * color is additionally modulated by a per-player sine-wave pulse to provide an animated, breathing effect.
 * <p>
 * For incomplete selections, only the positions that have already been set are rendered as isolated corner markers,
 * using the configured corner color.
 * <p>
 * To avoid excessive particle counts, rendering is skipped entirely for regions whose {@link BlockRegion#volume()}
 * exceeds the configured {@code maxRenderSize} threshold.
 * <p>
 * This implementation is not thread-safe. All calls to {@link #render} and {@link #clear} must be issued from the
 * server's main thread.
 *
 * @since 2.0
 */
public final class ParticleVisualizer implements SelectionVisualizer {

  // maximum number of edge particles per side for performance capping
  private static final int MAX_PARTICLES_PER_EDGE = 64;

  // particle step distance in blocks. lower = denser
  private static final double STEP = 0.5;

  private final Color edge;
  private final Color corner;
  private final double pulseSpeed;
  private final long maxRenderSize;

  // tracks the per-player pulse phase (0..2π)
  private final Map<UUID, Double> phaseMap = new HashMap<>();

  /**
   * Constructs a new {@code ParticleVisualizer} with the given visual parameters.
   *
   * @param edge          the color used for the twelve bounding-box edges
   * @param corner        the color used for the eight corner markers and incomplete-selection points
   * @param pulseSpeed    the amount by which the pulse phase advances per render call; higher values produce a faster
   *                      animation
   * @param maxRenderSize the maximum region {@link BlockRegion#volume() volume} for which particles are spawned;
   *                      regions larger than this threshold are skipped
   */
  public ParticleVisualizer(@NonNull Color edge, @NonNull Color corner, double pulseSpeed, long maxRenderSize) {
    this.edge = edge;
    this.corner = corner;
    this.pulseSpeed = pulseSpeed;
    this.maxRenderSize = maxRenderSize;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void render(@NonNull Player player, @NonNull Selection selection) {
    if (!selection.complete()) {
      selection.world().ifPresent(world -> {
        selection.pos1().ifPresent(position -> this.renderSinglePoint(player, world, position, this.corner));
        selection.pos2().ifPresent(position -> this.renderSinglePoint(player, world, position, this.corner));
      });
      return;
    }

    var world = selection.world().orElseThrow();
    var region = selection.toRegion();

    if (region.volume() > this.maxRenderSize) return;

    // advance pulse phase
    double phase = this.phaseMap.merge(player.getUniqueId(), this.pulseSpeed, Double::sum);
    float pulse = (float) (0.7 + 0.3 * Math.sin(phase)); // range 0.4 – 1.0

    this.renderEdges(player, world, region, pulse);
    this.renderCorners(player, world, region);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void clear(@NonNull Player player) {
    this.phaseMap.remove(player.getUniqueId());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Contract(pure = true)
  public @NonNull String type() {
    return "particle";
  }

  /**
   * Renders all twelve edges of the bounding box of the given region using dust particles.
   *
   * <p>The bounding box is expanded by one block on the positive axes so that particles align with the outer faces of
   * the corner blocks rather than their origins.
   *
   * @param player the player for whom particles are spawned
   * @param world  the world in which particles are spawned
   * @param region the region whose bounding box is drawn
   * @param alpha  a {@code [0, 1]} brightness factor applied to {@link #edge} via {@link #scaleColor(Color, float)}
   */
  private void renderEdges(@NonNull Player player, @NonNull World world, @NonNull BlockRegion region, float alpha) {
    var min = region.min();
    var max = region.max();

    double minX = min.x(), minY = min.y(), minZ = min.z();
    double maxX = max.x() + 1.0, maxY = max.y() + 1.0, maxZ = max.z() + 1.0;

    var color = this.scaleColor(this.edge, alpha);

    // bottom face
    this.renderLine(player, world, minX, minY, minZ, maxX, minY, minZ, color);
    this.renderLine(player, world, maxX, minY, minZ, maxX, minY, maxZ, color);
    this.renderLine(player, world, maxX, minY, maxZ, minX, minY, maxZ, color);
    this.renderLine(player, world, minX, minY, maxZ, minX, minY, minZ, color);

    // top face
    this.renderLine(player, world, minX, maxY, minZ, maxX, maxY, minZ, color);
    this.renderLine(player, world, maxX, maxY, minZ, maxX, maxY, maxZ, color);
    this.renderLine(player, world, maxX, maxY, maxZ, minX, maxY, maxZ, color);
    this.renderLine(player, world, minX, maxY, maxZ, minX, maxY, minZ, color);

    // vertical edges
    this.renderLine(player, world, minX, minY, minZ, minX, maxY, minZ, color);
    this.renderLine(player, world, maxX, minY, minZ, maxX, maxY, minZ, color);
    this.renderLine(player, world, maxX, minY, maxZ, maxX, maxY, maxZ, color);
    this.renderLine(player, world, minX, minY, maxZ, minX, maxY, maxZ, color);
  }

  /**
   * Spawns a large dust particle at each of the eight corners of the given region's bounding box using the configured
   * {@link #corner} color.
   *
   * @param player the player for whom particles are spawned
   * @param world  the world in which particles are spawned
   * @param region the region whose corners are highlighted
   */
  private void renderCorners(
    @NonNull Player player,
    @NonNull World world,
    @NonNull BlockRegion region
  ) {
    var min = region.min();
    var max = region.max();
    double minX = min.x(), minY = min.y(), minZ = min.z();
    double maxX = max.x() + 1.0, maxY = max.y() + 1.0, maxZ = max.z() + 1.0;

    this.spawnDust(player, world, minX, minY, minZ, this.corner, 1.5f);
    this.spawnDust(player, world, maxX, minY, minZ, this.corner, 1.5f);
    this.spawnDust(player, world, minX, maxY, minZ, this.corner, 1.5f);
    this.spawnDust(player, world, maxX, maxY, minZ, this.corner, 1.5f);
    this.spawnDust(player, world, minX, minY, maxZ, this.corner, 1.5f);
    this.spawnDust(player, world, maxX, minY, maxZ, this.corner, 1.5f);
    this.spawnDust(player, world, minX, maxY, maxZ, this.corner, 1.5f);
    this.spawnDust(player, world, maxX, maxY, maxZ, this.corner, 1.5f);
  }

  /**
   * Renders a small grid of dust particles around a single block position to indicate an incomplete-selection corner.
   *
   * <p>Particles are spawned at a 3×2×3 grid at half-block intervals on the XZ plane at {@code y + 0.5} and
   * {@code y + 1.0}, framing the top face of the block.
   *
   * @param player   the player for whom particles are spawned
   * @param world    the world in which particles are spawned
   * @param position the block position to mark
   * @param color    the dust color to use
   */
  private void renderSinglePoint(
    @NonNull Player player,
    @NonNull World world,
    @NonNull BlockVector3 position,
    @NonNull Color color
  ) {
    for (double dx = 0; dx <= 1; dx += 0.5) {
      for (double dz = 0; dz <= 1; dz += 0.5) {
        this.spawnDust(player, world, position.x() + dx, position.y() + 0.5, position.z() + dz, color, 1.0f);
        this.spawnDust(player, world, position.x() + dx, position.y() + 1.0, position.z() + dz, color, 1.0f);
      }
    }
  }

  /**
   * Spawns evenly-spaced dust particles along the straight line between two points.
   *
   * <p>The number of steps is capped at {@link #MAX_PARTICLES_PER_EDGE} to prevent excessive particle counts on very
   * long edges. Zero-length lines are silently ignored.
   *
   * @param player the player for whom particles are spawned
   * @param world  the world in which particles are spawned
   * @param x1     the x-coordinate of the line start
   * @param y1     the y-coordinate of the line start
   * @param z1     the z-coordinate of the line start
   * @param x2     the x-coordinate of the line end
   * @param y2     the y-coordinate of the line end
   * @param z2     the z-coordinate of the line end
   * @param color  the dust color to use
   */
  private void renderLine(
    @NonNull Player player,
    @NonNull World world,
    double x1, double y1, double z1,
    double x2, double y2, double z2,
    @NonNull Color color
  ) {
    double dx = x2 - x1, dy = y2 - y1, dz = z2 - z1;
    double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
    if (length == 0) return;

    int steps = (int) Math.min(Math.ceil(length / STEP), MAX_PARTICLES_PER_EDGE);
    double incX = dx / steps, incY = dy / steps, incZ = dz / steps;

    for (int i = 0; i <= steps; i++) {
      this.spawnDust(player, world, x1 + incX * i, y1 + incY * i, z1 + incZ * i, color, 0.8f);
    }
  }

  /**
   * Spawns a single {@link Particle#DUST} particle at the given world coordinates.
   *
   * @param player the player for whom the particle is spawned
   * @param world  the world in which the particle is spawned
   * @param x      the x-coordinate
   * @param y      the y-coordinate
   * @param z      the z-coordinate
   * @param color  the dust color
   * @param size   the visual size of the dust particle
   */
  private void spawnDust(
    @NonNull Player player,
    @NonNull World world,
    double x, double y, double z,
    @NonNull Color color,
    float size
  ) {
    var location = new Location(world, x, y, z);
    var dust = new Particle.DustOptions(color, size);
    player.spawnParticle(Particle.DUST, location, 1, 0, 0, 0, 0, dust);
  }

  /**
   * Returns a new {@link Color} whose RGB components are scaled by the given factor.
   *
   * <p>Component values are clamped implicitly to the {@code [0, 255]} range by integer truncation. The {@code factor}
   * should be in the range {@code [0.0, 1.0]} to produce valid RGB values.
   *
   * @param base   the base color to scale
   * @param factor the scaling factor applied to each RGB component
   * @return a new {@link Color} with scaled RGB components
   */
  @Contract(value = "_, _ -> new", pure = true)
  private @NonNull Color scaleColor(@NonNull Color base, float factor) {
    return Color.fromRGB(
      (int) (base.getRed() * factor),
      (int) (base.getGreen() * factor),
      (int) (base.getBlue() * factor)
    );
  }
}

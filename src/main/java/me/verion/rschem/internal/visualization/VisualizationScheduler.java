package me.verion.rschem.internal.visualization;

import lombok.NonNull;
import me.verion.rschem.selection.Selection;
import me.verion.rschem.session.EditSession;
import me.verion.rschem.session.SessionManager;
import me.verion.rschem.visualization.SelectionVisualizer;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Contract;

import java.util.logging.Level;

/**
 * Periodically renders the active {@link Selection} overlay for all online players whose visualization is enabled,
 * using a configured {@link SelectionVisualizer}.
 * <p>
 * The scheduler drives a single repeating {@link BukkitTask} that iterates over all active sessions on every tick
 * interval. Each session whose {@link EditSession#isVisualizationEnabled() visualization} and whose owner is online
 * receives a fresh render call. Rendering errors are caught per-player and logged as warnings so that a single
 * misbehaving session cannot interrupt rendering for other players.
 * <p>
 * The scheduler must be started explicitly via {@link #start()} and stopped via {@link #stop()}. {@link #stop()} should
 * be called during plugin shutdown to cancel the underlying task and release the reference.
 * <p>
 * All methods must be called from the server's main thread.
 *
 * @since 2.0
 */
public final class VisualizationScheduler {

  private final Plugin plugin;
  private final SessionManager sessionManager;
  private final SelectionVisualizer visualizer;
  private final long intervalTicks;

  private BukkitTask task;

  /**
   * Constructs a new {@code VisualizationScheduler} heartbeat.
   *
   * @param plugin         the owning plugin, used to register the repeating task
   * @param sessionManager the session manager whose sessions are iterated on each tick
   * @param visualizer     the visualizer used to render each player's selection
   * @param intervalTicks  the number of ticks between successive render passes; also used
   *                       as the initial delay before the first pass
   */
  public VisualizationScheduler(
    @NonNull Plugin plugin,
    @NonNull SessionManager sessionManager,
    @NonNull SelectionVisualizer visualizer,
    long intervalTicks
  ) {
    this.plugin = plugin;
    this.sessionManager = sessionManager;
    this.visualizer = visualizer;
    this.intervalTicks = intervalTicks;
  }

  /**
   * Starts the repeating render task if it is not already running.
   */
  public void start() {
    if (this.isRunning()) return;

    this.task = this.plugin.getServer().getScheduler().runTaskTimer(
      this.plugin,
      this::tick,
      this.intervalTicks,
      this.intervalTicks
    );
  }

  /**
   * Stops the repeating render task and releases the task reference.
   */
  public void stop() {
    if (this.task != null) {
      this.task.cancel();
      this.task = null;
    }
  }

  /**
   * Returns whether the render task is currently scheduled and running.
   *
   * @return {@code true} if a task is active and has not been canceled, {@code false} otherwise
   */
  @Contract(pure = true)
  public boolean isRunning() {
    return this.task != null && !this.task.isCancelled();
  }

  /**
   * Executes a single render pass over all active sessions.
   * <p>
   * Sessions whose visualization is disabled, or whose owner is offline, are skipped silently. Rendering exceptions are
   * caught per-player and logged as warnings to prevent a single failing session from interrupting the remaining render
   * calls.
   */
  private void tick() {
    for (var session : this.sessionManager.sessions()) {
      if (!session.isVisualizationEnabled()) continue;

      session.player().ifPresent(player -> {
        try {
          this.visualizer.render(player, session.selection());
        } catch (Exception exception) {
          this.plugin.getLogger().log(Level.WARNING, "visualization error for player " + player.getName(), exception);
        }
      });
    }
  }
}

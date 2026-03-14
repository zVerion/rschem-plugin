package me.verion.rschem;

import com.google.common.base.Preconditions;
import lombok.NonNull;
import me.verion.rschem.format.compression.CompressionType;
import me.verion.rschem.internal.visualization.ParticleVisualizer;
import me.verion.rschem.internal.visualization.VisualizationScheduler;
import me.verion.rschem.listener.InteractionListener;
import me.verion.rschem.schematic.SchematicService;
import me.verion.rschem.session.SessionManager;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Contract;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class RSchemPlugin extends JavaPlugin {

  private PluginConfiguration configuration;
  private SessionManager sessionManager;
  private SchematicService service;
  private VisualizationScheduler visualizationScheduler;

  @Override
  public void onEnable() {
    var start = System.currentTimeMillis();

    this.saveDefaultConfig();
    this.reloadConfig();

    try {
      this.configuration = PluginConfiguration.load(this);
      Files.createDirectories(this.configuration.directory());

      this.service = SchematicService.create(this);
      if (configuration.autoLoad()) {
        this.getLogger().info("[Rschem] Detecting schematics, please wait...");
        this.service.loadAll(this.configuration.directory());

        this.getLogger().info("[Rschem] Loaded "
          + this.service.registry().size()
          + " schematic(s) from " + this.configuration.directory().toAbsolutePath());
      }

      this.sessionManager = SessionManager.newSession();
      this.getServer().getPluginManager().registerEvents(new InteractionListener(this), this);

      this.getLogger().info("[Rschem] Starting Visualization heartbeat schedulers.");
      var edgeColor = Color.fromRGB(
        getConfig().getInt("visualization.edge-color.r", 255),
        getConfig().getInt("visualization.edge-color.g", 200),
        getConfig().getInt("visualization.edge-color.b", 0)
      );

      var cornerColor = Color.fromRGB(
        getConfig().getInt("visualization.corner-color.r", 255),
        getConfig().getInt("visualization.corner-color.g", 100),
        getConfig().getInt("visualization.corner-color.b", 0)
      );

      double pulseSpeed = getConfig().getDouble("visualization.pulse-speed", 0.10);
      long maxRenderSize = getConfig().getLong("visualization.max-render-size", 500L);
      long updateInterval = getConfig().getLong("visualization.update-interval-ticks", 10L);

      var visualizer = new ParticleVisualizer(edgeColor, cornerColor, pulseSpeed, maxRenderSize);

      this.visualizationScheduler = new VisualizationScheduler(this, this.sessionManager, visualizer, updateInterval);
      this.visualizationScheduler.start();

      var elapsed = System.currentTimeMillis() - start;
      this.getLogger().info("[Rschem] Successfully enabled Rschem in " + elapsed + " ms. Have fun.");
    } catch (Exception exception) {
      this.getLogger().log(Level.SEVERE, "[Rschem] Error while staring plugin: ", exception);
    }
  }

  @Override
  public void onDisable() {
    // stop the visualization task first to void NPEs on shutdown
    if (this.visualizationScheduler != null) {
      this.visualizationScheduler.stop();
    }

    // invalidate all sessions to release any held resources
    this.sessionManager.invalidateAll();
  }

  /**
   * Returns the loaded plugin configuration.
   *
   * @return the plugin configuration
   */
  @Contract(pure = true)
  public @NonNull PluginConfiguration configuration() {
    return this.configuration;
  }

  /**
   * Returns the active {@link SessionManager}.
   *
   * @return the session manager
   */
  @Contract(pure = true)
  public @NonNull SessionManager sessions() {
    return this.sessionManager;
  }

  /**
   * Returns the active {@link SchematicService}.
   *
   * @return the schematic service
   */
  @Contract(pure = true)
  public @NonNull SchematicService schematics() {
    return this.service;
  }

  /**
   * Immutable snapshot of all user-facing configuration values.
   *
   * <p> Created once during {@link #onEnable()} via {@link #load(RSchemPlugin)}. Invalid or missing values are
   * replaced with sensible defaults and a warning is emitted to the server log.
   *
   * @param directory        absolute path to the schematic storage directory
   * @param compression      compression algorithm used for schematic I/O
   * @param wandMaterial     material that acts as the region-selection wand
   * @param portToolMaterial material that acts as the port/paste tool
   * @param autoLoad         whether schematics are loaded from disk on startup
   */
  public record PluginConfiguration(
    @NonNull Path directory,
    @NonNull CompressionType compression,
    @NonNull Material wandMaterial,
    @NonNull Material portToolMaterial,
    boolean autoLoad
  ) {

    /**
     * Reads and validates all configuration values from {@code plugin}'s {@code config.yml}, falling back to documented
     * defaults for any missing or invalid entry.
     *
     * @param plugin the owning plugin, never {@code null}
     * @return a fully populated {@code PluginConfiguration}, never {@code null}
     */
    public static @NonNull PluginConfiguration load(@NonNull RSchemPlugin plugin) {
      Preconditions.checkNotNull(plugin, "plugin must not be null");

      var config = plugin.getConfig();
      var logger = plugin.getLogger();

      Path directory = plugin.getDataFolder().toPath().resolve(config.getString("schematic-directory", "schematics"));

      CompressionType compression = parseSafe(
        config.getString("compression", "ZSTD"),
        CompressionType.class,
        CompressionType.ZSTD,
        "compression",
        logger);

      Material wandMaterial = parseSafe(
        config.getString("wand-material", "GOLDEN_AXE"),
        Material.class,
        Material.GOLDEN_AXE,
        "wand-material",
        logger);

      Material portToolMaterial = parseSafe(
        config.getString("port-tool-material", "BLAZE_ROD"),
        Material.class,
        Material.BLAZE_ROD,
        "port-tool-material",
        logger);

      boolean autoLoad = config.getBoolean("auto-load", true);

      return new PluginConfiguration(
        directory,
        compression,
        wandMaterial,
        portToolMaterial,
        autoLoad
      );
    }

    /**
     * Attempts to resolve {@code value} as a constant of {@code enumType}. Returns {@code fallback} and emits a warning
     * if resolution fails.
     *
     * @param value    raw config string, may be mixed-case
     * @param type     target enum class
     * @param fallback value used when {@code value} is invalid
     * @param key      config key shown in the warning message
     * @param log      logger to write warnings to
     * @param <E>      enum type parameter
     * @return the resolved enum constant or {@code fallback}
     */
    private static <E extends Enum<E>> @NonNull E parseSafe(
      @NonNull String value,
      @NonNull Class<E> type,
      @NonNull E fallback,
      @NonNull String key,
      @NonNull Logger log
    ) {
      return Optional.of(value)
        .map(String::toUpperCase)
        .flatMap(v -> {
          try {
            return Optional.of(Enum.valueOf(type, v));
          } catch (IllegalArgumentException exception) {
            return Optional.empty();
          }
        }).orElseGet(() -> {
          log.warning("Invalid value for '" + key + "': '" + value + "' — falling back to " + fallback.name());
          return fallback;
        });
    }
  }
}

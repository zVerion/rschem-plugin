package me.verion.rschem.common.language.loader;

import lombok.NonNull;
import me.verion.rschem.common.language.I18nConfig;
import me.verion.rschem.common.language.exception.I18nInitializationException;
import me.verion.rschem.common.language.model.LoadedLanguage;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.UnmodifiableView;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.logging.Level;

/**
 * A {@link LanguageLoader} that discovers and loads {@code .properties} language files directly from the plugin's
 * JAR at runtime by scanning its entries under the configured resource path.
 * <p>
 * Each {@code .properties} file found under the configured resource prefix is loaded as a {@link LoadedLanguage},
 * with the file's base name (without extension) used as the locale code.
 *
 * @since 2.0
 */
public final class ClasspathLanguageLoader implements LanguageLoader {

  private static final String FILE_EXTENSION = ".properties";

  /**
   * Loads a single {@code .properties} language file from the plugin's classpath and inserts the resulting
   * {@link LoadedLanguage} into the given result map.
   * <p>
   * If the resource stream cannot be opened or the file cannot be parsed, the entry is skipped with a warning
   * rather than aborting the entire load cycle.
   *
   * @param plugin    the plugin whose classpath is used to locate the resource.
   * @param result    the map to insert the loaded language into.
   * @param entryName the full JAR entry path of the language file.
   * @param locale    the locale code derived from the file's base name.
   */
  private static void loadSingleLanguage(
    @NonNull JavaPlugin plugin,
    @NonNull Map<String, LoadedLanguage> result,
    @NonNull String entryName,
    @NonNull String locale
  ) {
    var resourceStream = plugin.getResource(entryName);
    if (resourceStream == null) {
      plugin.getLogger().warning("[I18n] Could not open resource stream for '" + entryName + "' — skipping.");
      return;
    }

    try (var reader = new InputStreamReader(resourceStream, StandardCharsets.UTF_8)) {
      var properties = new Properties();
      properties.load(reader);

      var messages = new HashMap<String, String>(properties.size());
      for (var entry : properties.entrySet()) {
        messages.put(entry.getKey().toString(), entry.getValue().toString());
      }

      result.put(locale, LoadedLanguage.of(locale, messages));
      plugin.getLogger().info("[I18n] Loaded '" + locale + "' — " + messages.size() + " message(s).");
    } catch (IOException exception) {
      plugin.getLogger().log(Level.WARNING, "[I18n] Failed to parse language file '" + entryName + "'.", exception);
    }
  }

  /**
   * Resolves and opens the JAR file from which the given plugin was loaded.
   *
   * @param plugin the plugin whose JAR file to resolve.
   * @return the opened {@link JarFile} for the plugin's JAR.
   * @throws I18nInitializationException if the JAR location cannot be determined or the file cannot be opened.
   */
  private static @NonNull JarFile resolveJarFile(@NonNull JavaPlugin plugin) {
    var codeSource = plugin.getClass().getProtectionDomain().getCodeSource();
    if (codeSource == null) {
      throw new I18nInitializationException("Cannot determine JAR location for plugin '" + plugin.getName() + "'");
    }

    try {
      return new JarFile(new File(codeSource.getLocation().toURI()));
    } catch (URISyntaxException | IOException exception) {
      throw new I18nInitializationException("Failed to open plugin JAR for language scanning", exception);
    }
  }

  /**
   * Ensures the given resource path ends with a trailing {@code /}, as required for JAR entry prefix matching.
   *
   * @param path the raw resource path to normalize.
   * @return the normalized path with a guaranteed trailing {@code /}.
   */
  private static @NonNull String normalizeResourcePrefix(@NonNull String path) {
    return path.endsWith("/") ? path : path + "/";
  }

  @Override
  public @NonNull @UnmodifiableView Map<String, LoadedLanguage> loadAll(
    @NonNull JavaPlugin plugin,
    @NonNull I18nConfig config
  ) {
    var resourcePrefix = normalizeResourcePrefix(config.languagesResourcePath());
    var result = new HashMap<String, LoadedLanguage>();

    try (var jarFile = resolveJarFile(plugin)) {
      var entries = jarFile.entries();

      while (entries.hasMoreElements()) {
        var entry = entries.nextElement();
        var entryName = entry.getName();
        if (!entryName.startsWith(resourcePrefix) || !entryName.endsWith(FILE_EXTENSION)) {
          continue;
        }

        var fileName = entryName.substring(entryName.lastIndexOf('/') + 1);

        var locale = fileName.substring(0, fileName.length() - FILE_EXTENSION.length());
        if (locale.isBlank()) {
          plugin.getLogger().warning("[I18n] Skipping malformed language entry (blank locale): " + entryName);
          continue;
        }

        loadSingleLanguage(plugin, result, entryName, locale);
      }
    } catch (IOException exception) {
      throw new I18nInitializationException("Failed to scan plugin JAR for language files", exception);
    }

    if (result.isEmpty()) {
      plugin.getLogger().warning("[I18n] No language files found under '" + resourcePrefix
        + "' in the plugin JAR. Ensure your .properties files are placed in "
        + "src/main/resources/" + config.languagesResourcePath() + "/");
    } else {
      plugin.getLogger().info("[I18n] Loaded " + result.size() + " language(s): " + result.keySet());
    }
    return Collections.unmodifiableMap(result);
  }
}

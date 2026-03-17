package me.verion.rschem.common.language.loader;

import lombok.NonNull;
import me.verion.rschem.common.language.I18nConfig;
import me.verion.rschem.common.language.exception.I18nInitializationException;
import me.verion.rschem.common.language.model.LoadedLanguage;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Map;

/**
 * Loads all available language files for a given plugin and returns them as an unmodifiable map keyed by locale code.
 *
 * @since 2.0
 */
public interface LanguageLoader {

  /**
   * Loads all language files defined by the given {@link I18nConfig} and returns them as an unmodifiable map of
   * locale code as a {@link LoadedLanguage}.
   *
   * @param plugin the plugin whose resources and data folder are used to locate language files.
   * @param config the i18n configuration describing which locales to load and where to find them.
   * @return an unmodifiable map of all successfully loaded languages.
   * @throws NullPointerException        if the given plugin or config is null.
   * @throws I18nInitializationException if any required language file cannot be read or parsed.
   */
  @NonNull
  @UnmodifiableView
  Map<String, LoadedLanguage> loadAll(@NonNull JavaPlugin plugin, @NonNull I18nConfig config);
}

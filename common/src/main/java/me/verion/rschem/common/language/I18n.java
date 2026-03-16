package me.verion.rschem.common.language;

import lombok.NonNull;
import me.verion.rschem.common.language.exception.I18nInitializationException;
import me.verion.rschem.common.language.format.MiniMessageFormatter;
import me.verion.rschem.common.language.loader.ClasspathLanguageLoader;
import me.verion.rschem.common.language.loader.LanguageLoader;
import me.verion.rschem.common.language.registry.DefaultLanguageRegistry;
import me.verion.rschem.common.language.registry.LanguageRegistry;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.Locale;

/**
 * The central entry point for the internationalization system. Provides static factory methods for resolving
 * localised {@link Component} messages by {@link Player}, {@link Locale}, or raw locale code.
 * <p>
 * The system must be initialized once during plugin startup via one of the {@code initialize} overloads before any
 * {@code get} or {@code reload} calls are made.
 *
 * @since 2.0
 */
public final class I18n {

  @VisibleForTesting
  static I18nService instance;

  private I18n() {
    throw new UnsupportedOperationException();
  }

  /**
   * Initializes the I18n system using the {@link ClasspathLanguageLoader} and a {@link DefaultLanguageRegistry}.
   *
   * @param plugin the owning plugin.
   * @param config the i18n configuration to apply.
   * @throws NullPointerException if the given plugin or config is null.
   */
  public static void initialize(@NonNull JavaPlugin plugin, @NonNull I18nConfig config) {
    initialize(plugin, config, new ClasspathLanguageLoader());
  }

  /**
   * Initializes the I18n system using the given {@link LanguageLoader} and a {@link DefaultLanguageRegistry}.
   *
   * @param plugin the owning plugin.
   * @param config the i18n configuration to apply.
   * @param loader the loader used to read language files.
   * @throws NullPointerException if any of the given arguments is null.
   */
  public static void initialize(
    @NonNull JavaPlugin plugin,
    @NonNull I18nConfig config,
    @NonNull LanguageLoader loader
  ) {
    initialize(plugin, config, loader, new DefaultLanguageRegistry(config.defaultLocale()));
  }

  /**
   * Initializes the I18n system with full control over the loader, registry, and formatter.
   *
   * @param plugin   the owning plugin.
   * @param config   the i18n configuration to apply.
   * @param loader   the loader used to read language files.
   * @param registry the registry to populate with the loaded languages.
   * @throws NullPointerException if any of the given arguments is null.
   */
  public static void initialize(
    @NonNull JavaPlugin plugin,
    @NonNull I18nConfig config,
    @NonNull LanguageLoader loader,
    @NonNull LanguageRegistry registry
  ) {
    var languages = loader.loadAll(plugin, config);
    registry.registerAll(languages);
    instance = new I18nService(registry, new MiniMessageFormatter(), plugin.getLogger());
  }

  /**
   * Resolves the message for the given key using the locale of the given {@link Player}.
   *
   * @param player the player whose locale is used for resolution.
   * @param key    the message key to resolve.
   * @return the resolved and rendered {@link Component}.
   * @throws NullPointerException        if the given player or key is null.
   * @throws I18nInitializationException if the I18n system has not been initialized.
   */
  public static @NonNull Component get(@NonNull Player player, @NonNull String key) {
    return service().resolve(player, key);
  }

  /**
   * Resolves the message for the given key using the locale of the given {@link Player}, applying the given
   * placeholders before rendering.
   *
   * @param player       the player whose locale is used for resolution.
   * @param key          the message key to resolve.
   * @param placeholders the placeholders to apply to the resolved message.
   * @return the resolved and rendered {@link Component}.
   * @throws NullPointerException        if the given player, key, or placeholders array is null.
   * @throws I18nInitializationException if the I18n system has not been initialized.
   */
  public static @NonNull Component get(
    @NonNull Player player,
    @NonNull String key,
    @NonNull Placeholder @NonNull ... placeholders
  ) {
    return service().resolve(player, key, placeholders);
  }

  /**
   * Resolves the message for the given key using the given {@link Locale}.
   *
   * @param locale the locale to use for resolution.
   * @param key    the message key to resolve.
   * @return the resolved and rendered {@link Component}.
   * @throws NullPointerException        if the given locale or key is null.
   * @throws I18nInitializationException if the I18n system has not been initialized.
   */
  public static @NonNull Component get(@NonNull Locale locale, @NonNull String key) {
    return service().resolve(locale, key);
  }

  /**
   * Resolves the message for the given key using the given {@link Locale}, applying the given placeholders before
   * rendering.
   *
   * @param locale       the locale to use for resolution.
   * @param key          the message key to resolve.
   * @param placeholders the placeholders to apply to the resolved message.
   * @return the resolved and rendered {@link Component}.
   * @throws NullPointerException        if the given locale, key, or placeholders array is null.
   * @throws I18nInitializationException if the I18n system has not been initialized.
   */
  public static @NonNull Component get(
    @NonNull Locale locale,
    @NonNull String key,
    @NonNull Placeholder @NonNull ... placeholders
  ) {
    return service().resolve(locale, key, placeholders);
  }

  /**
   * Resolves the message for the given key using the given raw locale code.
   *
   * @param locale the raw locale code to use for resolution (e.g. {@code "en_US"}).
   * @param key    the message key to resolve.
   * @return the resolved and rendered {@link Component}.
   * @throws NullPointerException        if the given locale or key is null.
   * @throws I18nInitializationException if the I18n system has not been initialized.
   */
  public static @NonNull Component get(@NonNull String locale, @NonNull String key) {
    return service().resolve(locale, key);
  }

  /**
   * Resolves the message for the given key using the given raw locale code, applying the given placeholders before
   * rendering.
   *
   * @param locale       the raw locale code to use for resolution (e.g. {@code "en_US"}).
   * @param key          the message key to resolve.
   * @param placeholders the placeholders to apply to the resolved message.
   * @return the resolved and rendered {@link Component}.
   * @throws NullPointerException        if the given locale, key, or placeholders array is null.
   * @throws I18nInitializationException if the I18n system has not been initialized.
   */
  public static @NonNull Component get(
    @NonNull String locale,
    @NonNull String key,
    @NonNull Placeholder @NonNull ... placeholders
  ) {
    return service().resolve(locale, key, placeholders);
  }

  /**
   * Reloads all language files from the classpath and clears the formatter's cache.
   *
   * @param plugin the owning plugin.
   * @param config the i18n configuration to reload from.
   * @throws NullPointerException        if the given plugin or config is null.
   * @throws I18nInitializationException if the I18n system has not been initialized.
   */
  public static void reload(@NonNull JavaPlugin plugin, @NonNull I18nConfig config) {
    var service = service();
    var languages = new ClasspathLanguageLoader().loadAll(plugin, config);

    service.registry().registerAll(languages);
    service.formatter().clearCache();

    plugin.getLogger().info("[I18n] Reloaded " + languages.size() + " language(s).");
  }

  /**
   * Returns the default locale code configured for this I18n system.
   *
   * @return the default locale code.
   * @throws I18nInitializationException if the I18n system has not been initialized.
   */
  public static @NonNull String defaultLocale() {
    return service().registry().defaultLocale();
  }

  /**
   * Get the singleton instance of the current {@link I18nService}
   *
   * @return the active service instance.
   */
  public static @NonNull I18nService service() {
    return I18n.instance;
  }
}

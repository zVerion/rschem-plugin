package me.verion.rschem.common.language;

import lombok.NonNull;
import me.verion.rschem.common.language.format.MessageFormatter;
import me.verion.rschem.common.language.registry.LanguageRegistry;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.logging.Logger;

/**
 * The internal resolution engine of the I18n system. Delegates message lookups to the {@link LanguageRegistry} and
 * renders the resolved raw string through the {@link MessageFormatter}.
 * <p>
 * Missing translation keys are logged as warnings and rendered as a clearly visible fallback component rather than
 * throwing, to avoid disrupting the server for a missing message.
 *
 * @since 2.0
 */
public final class I18nService {

  private final LanguageRegistry registry;
  private final MessageFormatter formatter;
  private final Logger logger;

  /**
   * Creates a new {@link I18nService}.
   *
   * @param registry  the registry used to look up raw message strings.
   * @param formatter the formatter used to render raw strings into {@link Component}s.
   * @param logger    the logger used to report missing translation keys.
   * @throws NullPointerException if any of the given arguments is null.
   */
  public I18nService(
    @NonNull LanguageRegistry registry,
    @NonNull MessageFormatter formatter,
    @NonNull Logger logger
  ) {
    this.registry = registry;
    this.formatter = formatter;
    this.logger = logger;
  }

  /**
   * Converts the given {@link Locale} into a locale code string of the form {@code language_COUNTRY}, or just
   * {@code language} if no country is set.
   *
   * @param locale the locale to convert.
   * @return the locale code string.
   * @throws NullPointerException if the given locale is null.
   */
  static @NonNull String toLocaleCode(@NonNull Locale locale) {
    var language = locale.getLanguage();
    var country = locale.getCountry();
    return country.isEmpty() ? language : language + '_' + country;
  }

  /**
   * Resolves the message for the given key using the given raw locale code, applying the given placeholders before
   * rendering. If the key is absent for the locale and all fallbacks, a visible fallback component is returned.
   *
   * @param locale       the raw locale code to use for resolution (e.g. {@code "en_US"}).
   * @param key          the message key to resolve.
   * @param placeholders the placeholders to apply to the resolved message.
   * @return the resolved and rendered {@link Component}.
   * @throws NullPointerException if the given locale, key, or placeholders array is null.
   */
  public @NonNull Component resolve(
    @NonNull String locale,
    @NonNull String key,
    @NonNull Placeholder @NonNull ... placeholders
  ) {
    var raw = this.registry.resolveMessage(locale, key);
    if (raw.isEmpty()) {
      this.logger.warning("[I18n] Missing translation key '" + key + "' for locale '"
        + locale + "' (and all fallbacks). Add it to your .properties files.");
      return this.formatter.format("<red>[MISSING: " + key + "]</red>");
    }
    return this.formatter.format(raw.get(), placeholders);
  }

  /**
   * Resolves the message for the given key using the given {@link Locale}, applying the given placeholders before
   * rendering.
   *
   * @param locale       the locale to use for resolution.
   * @param key          the message key to resolve.
   * @param placeholders the placeholders to apply to the resolved message.
   * @return the resolved and rendered {@link Component}.
   * @throws NullPointerException if the given locale, key, or placeholders array is null.
   */
  public @NonNull Component resolve(
    @NonNull Locale locale,
    @NonNull String key,
    @NonNull Placeholder @NonNull ... placeholders
  ) {
    return this.resolve(toLocaleCode(locale), key, placeholders);
  }

  /**
   * Resolves the message for the given key using the locale of the given {@link Player}, applying the given
   * placeholders before rendering.
   *
   * @param player       the player whose locale is used for resolution.
   * @param key          the message key to resolve.
   * @param placeholders the placeholders to apply to the resolved message.
   * @return the resolved and rendered {@link Component}.
   * @throws NullPointerException if the given player, key, or placeholders array is null.
   */
  public @NonNull Component resolve(
    @NonNull Player player,
    @NonNull String key,
    @NonNull Placeholder @NonNull ... placeholders
  ) {
    return this.resolve(player.locale(), key, placeholders);
  }

  /**
   * Returns the {@link LanguageRegistry} backing this service.
   *
   * @return the language registry.
   */
  public @NonNull LanguageRegistry registry() {
    return this.registry;
  }

  /**
   * Returns the {@link MessageFormatter} backing this service.
   *
   * @return the message formatter.
   */
  public @NonNull MessageFormatter formatter() {
    return this.formatter;
  }
}

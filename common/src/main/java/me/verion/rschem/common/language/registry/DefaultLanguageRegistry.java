package me.verion.rschem.common.language.registry;

import lombok.NonNull;
import me.verion.rschem.common.language.model.LoadedLanguage;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The default {@link LanguageRegistry} implementation, backed by a {@link ConcurrentHashMap} for thread-safe access.
 * <p>
 * Message resolution follows a three-step fallback chain:
 * <ol>
 *   <li>Exact locale match (e.g. {@code "en_US"}).
 *   <li>Language-only prefix match (e.g. {@code "en"}).
 *   <li>Default locale fallback (e.g. {@code "en"}).
 * </ol>
 *
 * @since 2.0
 */
public final class DefaultLanguageRegistry implements LanguageRegistry {

  private final String defaultLocale;
  private final ConcurrentHashMap<String, LoadedLanguage> languages = new ConcurrentHashMap<>();

  /**
   * Creates a new {@link DefaultLanguageRegistry} with the given default locale.
   *
   * @param defaultLocale the locale code to fall back to when a requested locale or key is not found.
   * @throws NullPointerException if the given default locale is null.
   */
  public DefaultLanguageRegistry(@NonNull String defaultLocale) {
    this.defaultLocale = defaultLocale;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void registerAll(@NonNull Map<String, LoadedLanguage> languages) {
    this.languages.clear();
    this.languages.putAll(languages);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull Optional<String> resolveMessage(@NonNull String locale, @NonNull String key) {
    var message = this.lookupInLanguage(locale, key);
    if (message.isPresent()) {
      return message;
    }

    var separatorIndex = locale.indexOf('_');
    if (separatorIndex > 0) {
      message = this.lookupInLanguage(locale.substring(0, separatorIndex), key);
      if (message.isPresent()) {
        return message;
      }
    }

    if (!locale.equals(this.defaultLocale)) {
      message = this.lookupInLanguage(this.defaultLocale, key);
      if (message.isPresent()) {
        return message;
      }
    }

    return Optional.empty();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull String defaultLocale() {
    return this.defaultLocale;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull @UnmodifiableView Map<String, LoadedLanguage> languages() {
    return Collections.unmodifiableMap(this.languages);
  }

  /**
   * Looks up the message for the given key in the language registered under the given locale code.
   *
   * @param locale the locale code to look up.
   * @param key    the message key to resolve.
   * @return an {@link Optional} containing the message, or an empty {@link Optional} if the locale or key is absent.
   */
  private @NonNull Optional<String> lookupInLanguage(@NonNull String locale, @NonNull String key) {
    var language = this.languages.get(locale);
    return language != null ? language.message(key) : Optional.empty();
  }
}

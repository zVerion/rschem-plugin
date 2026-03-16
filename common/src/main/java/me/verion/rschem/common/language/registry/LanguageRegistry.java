package me.verion.rschem.common.language.registry;

import lombok.NonNull;
import me.verion.rschem.common.language.I18n;
import me.verion.rschem.common.language.model.LoadedLanguage;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Map;
import java.util.Optional;

/**
 * Manages the set of loaded languages available to {@link I18n} and resolves message keys against a specific locale.
 *
 * @since 2.0
 */
public interface LanguageRegistry {

  /**
   * Registers all given languages into this registry, replacing any existing entries with the same locale code.
   *
   * @param languages a map of locale code → {@link LoadedLanguage} to register.
   * @throws NullPointerException if the given map is null.
   */
  void registerAll(@NonNull Map<String, LoadedLanguage> languages);

  /**
   * Resolves the message associated with the given key for the given locale. If the locale is not registered or the
   * key is absent for that locale, an empty {@link Optional} is returned.
   *
   * @param locale the locale code to look up (e.g. {@code "en_US"}).
   * @param key    the message key to resolve.
   * @return an {@link Optional} containing the resolved message, or an empty {@link Optional} if not found.
   * @throws NullPointerException if the given locale or key is null.
   */
  @NonNull
  Optional<String> resolveMessage(@NonNull String locale, @NonNull String key);

  /**
   * Returns the default locale code used as the fallback when a requested locale is not available.
   *
   * @return the default locale code; never {@code null}.
   */
  @NonNull
  String defaultLocale();

  /**
   * Returns an unmodifiable view of all languages currently held in this registry, keyed by locale code.
   *
   * @return an unmodifiable map of locale code → {@link LoadedLanguage}; never {@code null}.
   */
  @NonNull
  @UnmodifiableView
  Map<String, LoadedLanguage> languages();
}

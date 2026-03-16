package me.verion.rschem.common.language.model;

import lombok.NonNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Map;
import java.util.Optional;

/**
 * An immutable snapshot of a single loaded language, holding its locale code and the full set of resolved message
 * entries.
 *
 * @param locale   the locale code this language represents (e.g. {@code "en_US"}).
 * @param messages an unmodifiable map of message key → message value.
 * @since 2.0
 */
public record LoadedLanguage(@NonNull String locale, @NonNull @UnmodifiableView Map<String, String> messages) {

  /**
   * Creates a new {@link LoadedLanguage} from the given locale code and message map.
   *
   * @param locale   the locale code this language represents (e.g. {@code "en_US"}).
   * @param messages the raw message entries to store.
   * @return a new immutable {@link LoadedLanguage} instance.
   * @throws NullPointerException if the given locale or messages map is null.
   */
  public static @NonNull LoadedLanguage of(@NonNull String locale, @NonNull Map<String, String> messages) {
    return new LoadedLanguage(locale, messages);
  }

  /**
   * Looks up the message associated with the given key.
   *
   * @param key the message key to look up.
   * @return an {@link Optional} containing the message, or an empty {@link Optional} if no message exists for the key.
   * @throws NullPointerException if the given key is null.
   */
  public @NonNull Optional<String> message(@NonNull String key) {
    return Optional.ofNullable(this.messages.get(key));
  }

  /**
   * Returns whether a message with the given key is present in this language.
   *
   * @param key the message key to check.
   * @return {@code true} if a message exists for the given key, {@code false} otherwise.
   * @throws NullPointerException if the given key is null.
   */
  public boolean contains(@NonNull String key) {
    return this.messages.containsKey(key);
  }

  /**
   * Returns the total number of messages held in this language.
   *
   * @return the number of message entries.
   */
  public int size() {
    return this.messages.size();
  }
}

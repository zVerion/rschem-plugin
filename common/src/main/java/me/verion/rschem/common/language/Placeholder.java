package me.verion.rschem.common.language;

import lombok.NonNull;

/**
 * An immutable record representing a single named placeholder used for message template substitution.
 * <p>
 * Placeholders are resolved at format time by replacing angle-bracket tokens in a raw message string with the string
 * representation of the associated value.
 *
 * @param key   the placeholder key, without surrounding angle brackets (e.g. {@code "player"} or {@code "x"}).
 * @param value the value to substitute; {@link Object#toString()} is invoked at format time.
 * @since 2.0
 */
public record Placeholder(@NonNull String key, @NonNull Object value) {

  /**
   * Creates a new {@link Placeholder} with the given key and value.
   *
   * @param key   the placeholder key, without surrounding angle brackets.
   * @param value the value to substitute at format time.
   * @return a new {@link Placeholder} instance.
   * @throws NullPointerException if the given key or value is null.
   */
  public static @NonNull Placeholder of(@NonNull String key, @NonNull Object value) {
    return new Placeholder(key, value);
  }
}

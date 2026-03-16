package me.verion.rschem.command.flag;

import lombok.NonNull;

/**
 * Represents a parsed command flag; either a standalone boolean toggle or a key-value pair carrying an explicit
 * argument. Flags are prefixed with {@code --} in the raw input and extracted before argument parsing begins.
 *
 * @since 2.0
 */
public sealed interface Flag permits Flag.BooleanFlag, Flag.ValueFlag {

  /**
   * Returns the name of this flag as declared in the raw input, without the leading {@code --} prefix.
   *
   * @return the non-empty flag key.
   */
  @NonNull String key();

  /**
   * A {@link Flag} that carries no value and acts as a boolean toggle. Its presence indicates {@code true};
   * its absence indicates {@code false}.
   *
   * @param key the name of the flag without the leading {@code --} prefix.
   * @since 2.0
   */
  record BooleanFlag(@NonNull String key) implements Flag {

  }

  /**
   * A {@link Flag} that carries an explicit string value supplied after the flag key in the raw input.
   *
   * @param key   the name of the flag without the leading {@code --} prefix.
   * @param value the raw string value associated with this flag.
   * @since 2.0
   */
  record ValueFlag(@NonNull String key, @NonNull String value) implements Flag {

  }
}

package me.verion.rschem.internal.command.parser;

import lombok.NonNull;
import me.verion.rschem.internal.command.context.CommandSource;
import me.verion.rschem.internal.command.exception.ArgumentParseException;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

/**
 * Converts a raw string token into a strongly-typed value {@code T}.
 *
 * @param <T> the type this parser produces
 */
public interface ArgumentParser<T> {

  /**
   * Parses {@code input} into an instance of {@code T}.
   *
   * @param source the command source that triggered parsing
   * @param input  the raw string token to parse
   * @return the parsed value
   * @throws ArgumentParseException if {@code input} cannot be converted to {@code T}
   */
  @NonNull
  T parse(@NonNull CommandSource source, @NonNull String input) throws ArgumentParseException;

  /**
   * Provides tab-completion suggestions for the current partial input.
   *
   * @param source       the command source requesting completions
   * @param partialInput the text the player has typed so far
   * @return an immutable list of completion suggestions
   */
  @Unmodifiable
  default @NonNull List<String> suggest(@NonNull CommandSource source, @NonNull String partialInput) {
    return List.of();
  }
}

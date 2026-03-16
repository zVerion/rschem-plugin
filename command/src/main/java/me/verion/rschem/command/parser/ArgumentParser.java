package me.verion.rschem.command.parser;

import lombok.NonNull;
import me.verion.rschem.command.context.CommandSource;
import me.verion.rschem.command.exception.ArgumentParseException;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

/**
 * Converts a raw string token into a strongly-typed value of type {@code T}.
 * <p>
 * Implementations are registered against a target type and are invoked by the command framework during argument
 * resolution. Each parser is responsible for both parsing and providing tab-completion suggestions for its type.
 *
 * @param <T> the type this parser produces.
 * @since 2.0
 */
public interface ArgumentParser<T> {

  /**
   * Parses the given raw input string into an instance of {@code T}.
   *
   * @param source the command source that triggered parsing.
   * @param input  the raw string token to parse.
   * @return the parsed, non-null value.
   * @throws ArgumentParseException if the given input cannot be converted to {@code T}.
   * @throws NullPointerException   if the given source or input is null.
   */
  @NonNull
  T parse(@NonNull CommandSource source, @NonNull String input) throws ArgumentParseException;

  /**
   * Provides tab-completion suggestions for the current partial input. Returns an empty list by default.
   *
   * @param source       the command source requesting completions.
   * @param partialInput the text typed so far for this argument.
   * @return an unmodifiable list of completion suggestions.
   * @throws NullPointerException if the given source or partialInput is null.
   */
  @Unmodifiable
  default @NonNull List<String> suggest(@NonNull CommandSource source, @NonNull String partialInput) {
    return List.of();
  }
}

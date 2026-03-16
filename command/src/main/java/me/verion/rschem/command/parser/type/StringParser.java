package me.verion.rschem.command.parser.type;

import me.verion.rschem.command.context.CommandSource;
import me.verion.rschem.command.exception.ArgumentParseException;
import me.verion.rschem.command.parser.ArgumentParser;
import org.jspecify.annotations.NonNull;

/**
 * An {@link ArgumentParser} that returns the raw input string as-is, rejecting blank tokens.
 *
 * @since 2.0
 */
final class StringParser implements ArgumentParser<String> {

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull String parse(@NonNull CommandSource source, @NonNull String input) throws ArgumentParseException {
    if (input.isBlank()) {
      throw new ArgumentParseException("Expected a non-empty string, but received nothing");
    }
    return input;
  }
}

package me.verion.rschem.command.parser.type;

import me.verion.rschem.command.context.CommandSource;
import me.verion.rschem.command.exception.ArgumentParseException;
import me.verion.rschem.command.parser.ArgumentParser;
import org.jspecify.annotations.NonNull;

/**
 * An {@link ArgumentParser} that converts a raw string token into an {@link Integer}.
 *
 * @since 2.0
 */
final class IntegerParser implements ArgumentParser<Integer> {

  @Override
  public @NonNull Integer parse(@NonNull CommandSource source, @NonNull String input) throws ArgumentParseException {
    try {
      return Integer.parseInt(input);
    } catch (NumberFormatException ignored) {
      throw new ArgumentParseException("'" + input + "' is not a valid integer");
    }
  }
}

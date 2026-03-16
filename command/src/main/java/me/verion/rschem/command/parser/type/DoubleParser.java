package me.verion.rschem.command.parser.type;

import me.verion.rschem.command.context.CommandSource;
import me.verion.rschem.command.exception.ArgumentParseException;
import me.verion.rschem.command.parser.ArgumentParser;
import org.jspecify.annotations.NonNull;

/**
 * An {@link ArgumentParser} that converts a raw string token into a {@link Double}.
 *
 * @since 2.0
 */
final class DoubleParser implements ArgumentParser<Double> {

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull Double parse(@NonNull CommandSource source, @NonNull String input) throws ArgumentParseException {
    try {
      return Double.parseDouble(input);
    } catch (NumberFormatException ignored) {
      throw new ArgumentParseException("'" + input + "' is not a valid number");
    }
  }
}

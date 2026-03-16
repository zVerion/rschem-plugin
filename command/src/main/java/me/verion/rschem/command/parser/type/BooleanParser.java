package me.verion.rschem.command.parser.type;

import me.verion.rschem.command.context.CommandSource;
import me.verion.rschem.command.exception.ArgumentParseException;
import me.verion.rschem.command.parser.ArgumentParser;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Set;

/**
 * An {@link ArgumentParser} that converts a raw string token into a {@link Boolean}.
 *
 * @since 2.0
 */
final class BooleanParser implements ArgumentParser<Boolean> {

  private static final List<String> COMPLETIONS = List.of("true", "false");
  private static final Set<String> TRUTHY = Set.of("true", "yes", "on", "1");
  private static final Set<String> FALSY = Set.of("false", "no", "off", "0");

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull Boolean parse(@NonNull CommandSource source, @NonNull String input) throws ArgumentParseException {
    var lower = input.toLowerCase();
    if (TRUTHY.contains(lower)) {
      return Boolean.TRUE;
    }

    if (FALSY.contains(lower)) {
      return Boolean.FALSE;
    }

    throw new ArgumentParseException("'" + input + "' is not a valid boolean");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @Unmodifiable @NonNull List<String> suggest(@NonNull CommandSource source, @NonNull String partialInput) {
    return COMPLETIONS;
  }
}

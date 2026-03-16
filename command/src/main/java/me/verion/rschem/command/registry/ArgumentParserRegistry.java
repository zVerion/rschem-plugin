package me.verion.rschem.command.registry;

import lombok.NonNull;
import me.verion.rschem.command.parser.ArgumentParser;
import me.verion.rschem.command.parser.type.Parsers;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A registry that maps Java types to their corresponding {@link ArgumentParser} implementations.
 *
 * @since 2.0
 */
public final class ArgumentParserRegistry {

  private static final Map<Class<?>, ArgumentParser<?>> PARSERS = new HashMap<>();

  static {
    PARSERS.put(String.class, Parsers.STRING);
    PARSERS.put(Integer.class, Parsers.INTEGER);
    PARSERS.put(int.class, Parsers.INTEGER);
    PARSERS.put(Double.class, Parsers.DOUBLE);
    PARSERS.put(double.class, Parsers.DOUBLE);
    PARSERS.put(Boolean.class, Parsers.BOOLEAN);
    PARSERS.put(boolean.class, Parsers.BOOLEAN);
    PARSERS.put(Player.class, Parsers.ONLINE_PLAYER);
  }

  /**
   * Associates the given {@link ArgumentParser} with the given type, replacing any previously registered parser for
   * that type.
   *
   * @param type   the target type to register the parser for.
   * @param parser the parser to register.
   * @param <T>    the parsed value type.
   * @return this instance, for chaining.
   * @throws NullPointerException if the given type or parser is null.
   */
  public <T> @NonNull ArgumentParserRegistry register(@NonNull Class<T> type, @NonNull ArgumentParser<T> parser) {
    PARSERS.put(type, parser);
    return this;
  }

  /**
   * Looks up the {@link ArgumentParser} registered for the given type.
   *
   * @param type the type to look up.
   * @param <T>  the parsed value type.
   * @return an {@link Optional} containing the registered parser, or an empty {@link Optional} if none is registered.
   * @throws NullPointerException if the given type is null.
   */
  @SuppressWarnings("unchecked")
  public <T> @NonNull Optional<ArgumentParser<T>> find(@NonNull Class<T> type) {
    return Optional.ofNullable((ArgumentParser<T>) PARSERS.get(type));
  }

  /**
   * Looks up the {@link ArgumentParser} registered for the given type, throwing if none is registered.
   *
   * @param type the type to look up.
   * @param <T>  the parsed value type.
   * @return the registered parser.
   * @throws IllegalStateException if no parser is registered for the given type.
   * @throws NullPointerException  if the given type is null.
   */
  @SuppressWarnings("unchecked")
  public <T> @NonNull ArgumentParser<T> require(@NonNull Class<?> type) {
    var parser = PARSERS.get(type);
    if (parser == null) {
      throw new IllegalStateException("No ArgumentParser registered for type '" + type.getName() + "'");
    }
    return (ArgumentParser<T>) parser;
  }
}

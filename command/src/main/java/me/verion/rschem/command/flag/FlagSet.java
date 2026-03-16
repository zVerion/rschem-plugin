package me.verion.rschem.command.flag;

import lombok.NonNull;
import me.verion.rschem.command.exception.ArgumentParseException;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;

/**
 * An immutable, parsed collection of {@link Flag} entries extracted from a raw command input string.
 * <p>
 * Flags are identified by a leading {@code -} prefix. A token immediately following a flag key that does not itself
 * start with {@code -} is treated as the flag's value, producing a {@link Flag.ValueFlag}; otherwise a
 * {@link Flag.BooleanFlag} is recorded.
 *
 * @param flags an unmodifiable map of flag key → parsed {@link Flag}
 * @since 2.0
 */
public record FlagSet(@NonNull @UnmodifiableView Map<String, Flag> flags) {

  /**
   * A shared, empty {@link FlagSet} instance representing an input with no flags.
   */
  public static final FlagSet EMPTY = new FlagSet(Map.of());

  /**
   * Parses all flags from the given raw input string and returns them as a {@link FlagSet}.
   * <p>
   * Tokens starting with {@code -} are treated as flag keys. If the token immediately following a flag key does not
   * start with {@code -}, it is consumed as the flag's value and a {@link Flag.ValueFlag} is produced; otherwise a
   * {@link Flag.BooleanFlag} is recorded. All other tokens are silently skipped.
   *
   * @param raw the full raw input string to parse.
   * @return a new {@link FlagSet} containing all flags found in the input.
   * @throws NullPointerException if the given raw string is null.
   */
  public static @NonNull FlagSet parse(@NonNull String raw) {
    var tokens = raw.strip().split("\\s+");
    var result = new LinkedHashMap<String, Flag>();

    var index = 0;
    while (index < tokens.length) {
      var token = tokens[index];
      if (!token.startsWith("-") || token.length() < 2) {
        index++;
        continue;
      }

      var key = token.substring(1);
      var nextIndex = index + 1;

      if (nextIndex < tokens.length && !tokens[nextIndex].startsWith("-")) {
        result.put(key, new Flag.ValueFlag(key, tokens[nextIndex]));
        index += 2;
      } else {
        result.put(key, new Flag.BooleanFlag(key));
        index++;
      }
    }
    return new FlagSet(Map.copyOf(result));
  }

  /**
   * Returns whether a flag with the given key is present in this set.
   *
   * @param key the flag key to look up, without the leading {@code -} prefix.
   * @return {@code true} if the flag is present, {@code false} otherwise.
   * @throws NullPointerException if the given key is null.
   */
  public boolean has(@NonNull String key) {
    return this.flags.containsKey(key);
  }

  /**
   * Returns the string value carried by the {@link Flag.ValueFlag} with the given key, if present.
   *
   * @param key the flag key to look up, without the leading {@code -} prefix.
   * @return an {@link Optional} containing the flag's value, or an empty {@link Optional} if the flag is absent.
   * @throws IllegalStateException if the flag is present but is a {@link Flag.BooleanFlag} carrying no value.
   * @throws NullPointerException  if the given key is null.
   */
  public @NonNull Optional<String> value(@NonNull String key) {
    return switch (this.flags.get(key)) {
      case null -> Optional.empty();
      case Flag.BooleanFlag ignored ->
        throw new IllegalStateException("Flag '-" + key + "' carries no value; check with has() first");
      case Flag.ValueFlag(var ignored, var value) -> Optional.of(value);
    };
  }

  /**
   * Returns the integer value carried by the {@link Flag.ValueFlag} with the given key, if present.
   *
   * @param key the flag key to look up, without the leading {@code -} prefix.
   * @return an {@link Optional} containing the parsed integer value, or an empty {@link Optional} if the flag is absent.
   * @throws ArgumentParseException if the flag is present but its value cannot be parsed as an integer.
   * @throws IllegalStateException  if the flag is present but is a {@link Flag.BooleanFlag} carrying no value.
   * @throws NullPointerException   if the given key is null.
   */
  public @NonNull Optional<Integer> intValue(@NonNull String key) throws ArgumentParseException {
    var raw = this.value(key);
    if (raw.isEmpty()) {
      return Optional.empty();
    }

    try {
      return Optional.of(Integer.parseInt(raw.get()));
    } catch (NumberFormatException ignored) {
      throw new ArgumentParseException("Flag '-" + key + "' expects an integer, got '" + raw.get() + "'");
    }
  }
}

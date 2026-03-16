package me.verion.rschem.command.context;

import lombok.NonNull;
import me.verion.rschem.command.parser.ArgumentParser;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Map;

/**
 * An immutable snapshot of a single command invocation, carrying the originating {@link CommandSource} and all parsed
 * named arguments.
 * <p>
 * Argument values are stored and retrieved by their route-pattern names (the token between angle or square brackets).
 * Type-safety is the responsibility of the caller — each argument was already validated and converted by the matching
 * {@link ArgumentParser} before this record is constructed.
 *
 * @param source    the entity that issued the command.
 * @param arguments an immutable map consisting argument name and the parsed value.
 * @param raw       the full, unprocessed argument string as received from Bukkit.
 * @since 2.0
 */
public record CommandContext(
  @NonNull CommandSource source,
  @NonNull @UnmodifiableView Map<String, Object> arguments,
  @NonNull String raw
) {

  /**
   * Retrieves a parsed argument value by name and casts it to the expected type {@code T}.
   *
   * @param name the argument name as declared in the route pattern.
   * @param <T>  the target type.
   * @return the resolved argument value, or {@code null} if the argument was optional and absent.
   * @throws ClassCastException   if the stored value is incompatible with {@code T}.
   * @throws NullPointerException if the given name is null.
   */
  @SuppressWarnings("unchecked")
  public <T> @Nullable T argument(@NonNull String name) {
    return (T) this.arguments.get(name);
  }

  /**
   * Retrieves a required argument by name and casts it to the expected type {@code T}, throwing if it is absent.
   *
   * @param name the argument name as declared in the route pattern.
   * @param <T>  the target type.
   * @return the resolved, non-null argument value.
   * @throws IllegalStateException if no argument with the given name is present in the context.
   * @throws ClassCastException    if the stored value is incompatible with {@code T}.
   * @throws NullPointerException  if the given name is null.
   */
  @SuppressWarnings("unchecked")
  public <T> @NonNull T requireArgument(@NonNull String name) {
    var value = this.arguments.get(name);
    if (value == null) {
      throw new IllegalStateException("Required argument '" + name + "' is not present in the context");
    }
    return (T) value;
  }

  /**
   * Checks whether an optional argument with the given name was provided.
   *
   * @param name the argument name as declared in the route pattern.
   * @return {@code true} if a value for this argument is present, {@code false} otherwise.
   * @throws NullPointerException if the given name is null.
   */
  public boolean hasArgument(@NonNull String name) {
    return this.arguments.containsKey(name);
  }
}

package me.verion.rschem.command.exception;

import lombok.NonNull;
import me.verion.rschem.command.context.CommandSource;
import me.verion.rschem.command.parser.ArgumentParser;

/**
 * Thrown by an {@link ArgumentParser} when a raw input string cannot be converted into the expected target type.
 * <p>
 * The message should describe what was expected and what was received, as it may be forwarded to the
 * {@link CommandSource} as a user-facing error.
 *
 * @since 2.0
 */
public final class ArgumentParseException extends Exception {

  /**
   * Constructs a new {@link ArgumentParseException} with the given detail message.
   *
   * @param message a human-readable description of the parse failure.
   */
  public ArgumentParseException(@NonNull String message) {
    super(message);
  }

  /**
   * Constructs a new {@link ArgumentParseException} with the given detail message and underlying cause.
   *
   * @param message a human-readable description of the parse failure.
   * @param cause   the underlying exception that triggered this failure.
   */
  public ArgumentParseException(@NonNull String message, @NonNull Throwable cause) {
    super(message, cause);
  }
}

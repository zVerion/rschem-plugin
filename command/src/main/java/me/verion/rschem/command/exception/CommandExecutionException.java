package me.verion.rschem.command.exception;

import lombok.NonNull;

/**
 * Thrown when an unexpected, non-parse error occurs during command dispatching; for example when reflective method
 * invocation fails due to an uncaught exception in the handler method body.
 *
 * @since 2.0
 */
public final class CommandExecutionException extends RuntimeException {

  /**
   * Constructs a new {@link CommandExecutionException} wrapping the given underlying cause.
   *
   * @param cause the underlying exception that triggered this failure.
   * @throws NullPointerException if the given cause is null.
   */
  public CommandExecutionException(@NonNull Throwable cause) {
    super(cause);
  }

  /**
   * Constructs a new {@link CommandExecutionException} with the given detail message and underlying cause.
   *
   * @param message a human-readable description of the failure.
   * @param cause   the underlying exception that triggered this failure.
   * @throws NullPointerException if the given message or cause is null.
   */
  public CommandExecutionException(@NonNull String message, @NonNull Throwable cause) {
    super(message, cause);
  }
}

package me.verion.rschem.common.language.exception;

import lombok.NonNull;
import me.verion.rschem.common.language.I18n;
import org.jetbrains.annotations.Nullable;

/**
 * Thrown when the {@link I18n} system fails to initialize; for example when a required locale file is missing,
 * malformed, or cannot be read from disk.
 *
 * @since 2.0
 */
public final class I18nInitializationException extends RuntimeException {

  /**
   * Constructs a new {@link I18nInitializationException} with the given detail message.
   *
   * @param message a human-readable description of the failure cause.
   * @throws NullPointerException if the given message is null.
   */
  public I18nInitializationException(@NonNull String message) {
    super(message);
  }

  /**
   * Constructs a new {@link I18nInitializationException} with the given detail message and underlying cause.
   *
   * @param message a human-readable description of the failure cause.
   * @param cause   the underlying exception that triggered this failure.
   * @throws NullPointerException if the given message is null.
   */
  public I18nInitializationException(@NonNull String message, @Nullable Throwable cause) {
    super(message, cause);
  }
}

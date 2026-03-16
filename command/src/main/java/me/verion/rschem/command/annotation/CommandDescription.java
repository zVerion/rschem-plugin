package me.verion.rschem.command.annotation;

import lombok.NonNull;

import java.lang.annotation.*;

/**
 * Attaches a human-readable description to a {@link SubCommand}-annotated method.
 * Descriptions are surfaced in the auto-generated {@code /cmd help} output.
 *
 * @since 2.0
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CommandDescription {

  /**
   * A short, single-line description of what this sub-command does.
   *
   * @return the non-empty description string.
   */
  @NonNull String value();
}


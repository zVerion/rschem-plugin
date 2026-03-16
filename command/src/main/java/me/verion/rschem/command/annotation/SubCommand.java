package me.verion.rschem.command.annotation;

import lombok.NonNull;

import java.lang.annotation.*;

/**
 * Marks a method as a sub-command handler. The {@link #value()} defines the command route; literals are plain tokens,
 * required arguments are wrapped in {@code <angle brackets>}, and optional arguments in {@code [square brackets]}.
 *
 * @since 2.0
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SubCommand {

  /**
   * One or more command routes this handler accepts. Multiple values are treated as aliases and all resolve to the same
   * handler.
   *
   * @return the non-empty array of command route patterns.
   */
  @NonNull String @NonNull [] value();
}

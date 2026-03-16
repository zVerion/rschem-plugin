package me.verion.rschem.command.annotation;

import lombok.NonNull;

import java.lang.annotation.*;

/**
 * Binds a method parameter to a named argument declared in the {@link SubCommand} route. The {@link #value()} must
 * match exactly the token name used inside the angle or square brackets of the route pattern.
 *
 * @since 2.0
 */
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Arg {

  /**
   * The name of the argument as it appears in the route pattern, without surrounding brackets.
   *
   * @return the argument name.
   */
  @NonNull String value();
}

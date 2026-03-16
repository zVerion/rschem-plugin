package me.verion.rschem.command.annotation;

import lombok.NonNull;
import me.verion.rschem.command.context.CommandSource;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

/**
 * Declares the permission node required to execute a {@link SubCommand} annotated method or an entire command class.
 * When placed on a class, all sub-commands inside that class inherit the permission unless they declare their own.
 * <p>
 * If the executing {@link CommandSource} does not hold this permission, the framework refuses the execution and sends a
 * configurable no-permission message.
 *
 * @since 2.0
 */
@Documented
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface CommandPermission {

  /**
   * The permission node required to execute this command.
   *
   * @return the non-empty permission node.
   */
  @NonNull String value();
}

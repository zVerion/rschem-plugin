package me.verion.rschem.command.registry;

import lombok.NonNull;
import me.verion.rschem.command.annotation.SubCommand;
import me.verion.rschem.command.route.RegisteredHandler;
import me.verion.rschem.command.route.RouteParser;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Scans sub-command handler classes at registration time and stores the resulting {@link RegisteredHandler}s for fast
 * O(n) dispatch lookups.
 * <p>
 * The registry is populated once at plugin startup and thereafter treated as read-only by the dispatch pipeline.
 *
 * @since 2.0
 */
public final class CommandRegistry {

  private final List<RegisteredHandler> handlers = new ArrayList<>();

  /**
   * Scans all methods of the given handler instance's class and registers every method annotated with
   * {@link SubCommand} as a command handler.
   * <p>
   * Each alias declared in {@link SubCommand#value()} is registered as a separate {@link RegisteredHandler} so that
   * route matching works uniformly.
   *
   * @param handlerInstance the object whose methods to scan.
   * @return this instance, for chaining.
   * @throws IllegalArgumentException if any annotated method declares an invalid route pattern.
   * @throws NullPointerException     if the given handler instance is null.
   */
  public @NonNull CommandRegistry register(@NonNull Object handlerInstance) {
    for (var method : handlerInstance.getClass().getDeclaredMethods()) {
      var subCommand = method.getAnnotation(SubCommand.class);
      if (subCommand == null) {
        continue;
      }

      for (var pattern : subCommand.value()) {
        var nodes = RouteParser.parse(pattern);
        this.handlers.add(RegisteredHandler.of(nodes, method, handlerInstance));
      }
    }
    return this;
  }

  /**
   * Returns an unmodifiable view of all registered {@link RegisteredHandler}s, in the order they were registered.
   *
   * @return an unmodifiable view of all registered handlers; never {@code null}.
   */
  public @NonNull @UnmodifiableView List<RegisteredHandler> handlers() {
    return Collections.unmodifiableList(this.handlers);
  }
}

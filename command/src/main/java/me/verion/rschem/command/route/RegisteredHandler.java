package me.verion.rschem.command.route;

import lombok.NonNull;
import me.verion.rschem.command.annotation.Arg;
import me.verion.rschem.command.annotation.CommandDescription;
import me.verion.rschem.command.annotation.CommandPermission;
import me.verion.rschem.command.context.CommandContext;
import me.verion.rschem.command.exception.CommandExecutionException;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An internal, immutable binding between a parsed command route and the reflective {@link Method} that handles it.
 *
 * @param nodes       the ordered, immutable list of {@link RouteNode}s parsed from the route pattern.
 * @param method      the reflected handler method.
 * @param owner       the object instance on which to invoke {@code method}.
 * @param permission  the required permission node, or {@code null} if no permission is required.
 * @param description a short human-readable description, or {@code null} if none was declared.
 * @since 2.0
 */
public record RegisteredHandler(
  @NonNull List<RouteNode> nodes,
  @NonNull Method method,
  @NonNull Object owner,
  @Nullable String permission,
  @Nullable String description
) {

  /**
   * Constructs a {@link RegisteredHandler} by reading {@link CommandPermission} and {@link CommandDescription}
   * annotations directly from the given method, falling back to a class-level {@link CommandPermission} if no
   * method-level annotation is present.
   *
   * @param nodes  the pre-parsed route nodes for this handler.
   * @param method the reflective method to bind.
   * @param owner  the instance on which the method will be invoked.
   * @return a fully initialised {@link RegisteredHandler}.
   * @throws NullPointerException if any of the given arguments is null.
   */
  public static @NonNull RegisteredHandler of(
    @NonNull List<RouteNode> nodes,
    @NonNull Method method,
    @NonNull Object owner
  ) {
    var methodPermission = method.getAnnotation(CommandPermission.class);
    var classPermission = owner.getClass().getAnnotation(CommandPermission.class);
    var description = method.getAnnotation(CommandDescription.class);

    var permission = methodPermission != null ? methodPermission.value()
      : classPermission != null ? classPermission.value()
      : null;

    method.setAccessible(true);

    return new RegisteredHandler(
      List.copyOf(nodes),
      method,
      owner,
      permission,
      description != null ? description.value() : null
    );
  }

  /**
   * Resolves and invokes the handler method with arguments derived from the given {@link CommandContext}.
   * <p>
   * The first parameter is always bound to {@link CommandContext#source()}. All subsequent parameters are resolved
   * by their {@link Arg} annotation against the context's parsed argument map.
   *
   * @param context the dispatch context holding the command source and all parsed argument values.
   * @throws CommandExecutionException if reflective invocation fails for any reason.
   * @throws NullPointerException      if the given context is null.
   */
  public void invoke(@NonNull CommandContext context) {
    var params = this.method.getParameters();
    var args = new Object[params.length];

    args[0] = context.source();

    for (var index = 1; index < params.length; index++) {
      var param = params[index];
      var argAnnotation = param.getAnnotation(Arg.class);

      if (argAnnotation != null) {
        args[index] = context.arguments().get(argAnnotation.value());
      }
    }

    try {
      this.method.invoke(this.owner, args);
    } catch (InvocationTargetException | IllegalAccessException exception) {
      throw new CommandExecutionException(exception.getCause() != null ? exception.getCause() : exception);
    }
  }

  /**
   * Builds a human-readable usage string from the stored route nodes.
   *
   * @param rootLabel the root command label to prepend, without a leading {@code /}.
   * @return the full usage string, including the leading {@code /}.
   * @throws NullPointerException if the given root label is null.
   */
  public @NonNull String buildUsage(@NonNull String rootLabel) {
    var nodeTokens = this.nodes.stream()
      .map(RouteNode::token)
      .collect(Collectors.joining(" "));
    return "/" + rootLabel + (nodeTokens.isEmpty() ? "" : " " + nodeTokens);
  }

  /**
   * Returns whether this handler's route matches the given tokenized input. Matching is performed node-by-node;
   * optional tail nodes may be absent from the input without causing a mismatch.
   *
   * @param tokens the tokenized user input, with the root label already stripped.
   * @return {@code true} if this handler's route matches the given tokens, {@code false} otherwise.
   * @throws NullPointerException if the given tokens array is null.
   */
  public boolean matches(@NonNull String[] tokens) {
    var index = 0;

    for (var node : this.nodes) {
      if (node instanceof RouteNode.OptionalArgNode) {
        index++;
        continue;
      }

      if (index >= tokens.length || !node.matches(tokens[index])) {
        return false;
      }
      index++;
    }
    return index >= tokens.length || this.greedyTail();
  }

  /**
   * Returns whether the last argument node of this route should consume all remaining input tokens. This is the case
   * when the last parameter is declared as a {@link String}, indicating greedy matching behavior.
   *
   * @return {@code true} if the last argument of this route is a {@link String} parameter, {@code false} otherwise.
   */
  private boolean greedyTail() {
    if (this.nodes.isEmpty()) {
      return false;
    }

    var last = this.nodes.getLast();
    if (!(last instanceof RouteNode.RequiredArgNode || last instanceof RouteNode.OptionalArgNode)) {
      return false;
    }

    var params = this.method.getParameters();
    return params.length > 0 && params[params.length - 1].getType() == String.class;
  }
}

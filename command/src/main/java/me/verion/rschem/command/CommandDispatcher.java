package me.verion.rschem.command;

import lombok.NonNull;
import me.verion.rschem.command.context.CommandContext;
import me.verion.rschem.command.context.CommandSource;
import me.verion.rschem.command.exception.ArgumentParseException;
import me.verion.rschem.command.exception.CommandExecutionException;
import me.verion.rschem.command.parser.ArgumentParser;
import me.verion.rschem.command.registry.ArgumentParserRegistry;
import me.verion.rschem.command.registry.CommandRegistry;
import me.verion.rschem.command.route.RegisteredHandler;
import me.verion.rschem.command.route.RouteNode;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Resolves an incoming command invocation to the correct {@link RegisteredHandler}, parses its arguments, enforces
 * permission checks, and invokes the handler method.
 * <p>
 * The dispatcher is stateless beyond its constructor-injected dependencies and is therefore safe to use from any thread,
 * though Bukkit commands always fire on the main thread in practice.
 *
 * @since 2.0
 */
public final class CommandDispatcher {

  private final CommandRegistry registry;
  private final ArgumentParserRegistry parserRegistry;
  private final String root;

  /**
   * Constructs a new {@link CommandDispatcher}.
   *
   * @param registry       the registry containing all handlers to dispatch against.
   * @param parserRegistry the registry used to resolve argument parsers at dispatch-time.
   * @param root           the root command label, without a leading slash.
   * @throws NullPointerException if any of the given arguments is null.
   */
  public CommandDispatcher(
    @NonNull CommandRegistry registry,
    @NonNull ArgumentParserRegistry parserRegistry,
    @NonNull String root
  ) {
    this.registry = registry;
    this.parserRegistry = parserRegistry;
    this.root = root;
  }

  /**
   * Dispatches an execution request from the given {@link CommandSource} against the registered handlers.
   * <p>
   * The dispatch cycle proceeds as follows:
   * <ol>
   *   <li>Find the first {@link RegisteredHandler} whose route matches the given token sequence.
   *   <li>Check the required permission; send a no-permission message and return early if denied.
   *   <li>Parse each argument token through its registered {@link ArgumentParser}.
   *   <li>Build a {@link CommandContext} and invoke the handler method.
   * </ol>
   * If no matching handler is found, the auto-generated help listing is sent to the source instead.
   *
   * @param source the command source that issued the command.
   * @param args   the raw argument tokens as delivered by Bukkit, with the root label already stripped.
   * @throws NullPointerException if the given source or args array is null.
   */
  public void dispatch(@NonNull CommandSource source, @NonNull String[] args) {
    var handler = this.findHandler(args);
    if (handler == null) {
      this.sendHelp(source);
      return;
    }

    if (handler.permission() != null && !source.hasPermission(handler.permission())) {
      source.sendMessage("no-permission");
      return;
    }

    var parsed = new HashMap<String, Object>();
    try {
      this.parseArguments(handler, args, source, parsed);
    } catch (ArgumentParseException exception) {
      source.sendMessage("parse-error");
      return;
    }

    var context = new CommandContext(source, Collections.unmodifiableMap(parsed), String.join(" ", args));
    try {
      handler.invoke(context);
    } catch (CommandExecutionException exception) {
      source.sendMessage("error-execute");
      throw exception;
    }
  }

  /**
   * Computes tab-completion suggestions for the current partial input.
   * <p>
   * For each registered handler whose route prefix matches the already-typed tokens, the node at the current cursor
   * position contributes suggestions — either the literal token itself or parser-provided suggestions for argument nodes.
   * Handlers the source lacks permission for are silently skipped.
   *
   * @param source the command source requesting completions.
   * @param args   the current token sequence, including the partial last token.
   * @return a mutable list of completion strings.
   * @throws NullPointerException if the given source or args array is null.
   */
  public @NonNull List<String> complete(@NonNull CommandSource source, @NonNull String @NonNull [] args) {
    var completions = new ArrayList<String>();
    var currentIndex = args.length - 1;
    var partial = args.length > 0 ? args[currentIndex] : "";

    for (var handler : this.registry.handlers()) {
      if (handler.permission() != null && !source.hasPermission(handler.permission())) {
        continue;
      }

      var nodes = handler.nodes();
      if (currentIndex >= nodes.size()) {
        continue;
      }

      if (!this.prefixMatches(nodes, args, currentIndex)) {
        continue;
      }

      switch (nodes.get(currentIndex)) {
        case RouteNode.LiteralNode literal -> {
          if (literal.token().toLowerCase().startsWith(partial.toLowerCase())) {
            completions.add(literal.token());
          }
        }
        case RouteNode.RequiredArgNode ignored ->
          this.suggestFromParser(handler, currentIndex, source, partial, completions);
        case RouteNode.OptionalArgNode ignored ->
          this.suggestFromParser(handler, currentIndex, source, partial, completions);
      }
    }

    return completions;
  }

  /**
   * Returns the first {@link RegisteredHandler} whose route matches the given token sequence, or {@code null} if
   * no match is found.
   *
   * @param args the tokenized input to match against.
   * @return the matching handler, or {@code null} if none was found.
   */
  private @Nullable RegisteredHandler findHandler(@NonNull String[] args) {
    for (var handler : this.registry.handlers()) {
      if (handler.matches(args)) {
        return handler;
      }
    }
    return null;
  }

  /**
   * Returns whether the nodes at indices {@code 0} to {@code length - 1} all match the corresponding input tokens.
   *
   * @param nodes  the handler's route nodes.
   * @param args   the tokenized input.
   * @param length the number of prefix tokens to verify.
   * @return {@code true} if all prefix nodes match their corresponding input tokens, {@code false} otherwise.
   */
  private boolean prefixMatches(@NonNull List<RouteNode> nodes, @NonNull String[] args, int length) {
    for (var i = 0; i < length; i++) {
      if (i >= nodes.size() || !nodes.get(i).matches(args[i])) {
        return false;
      }
    }
    return true;
  }

  /**
   * Iterates over the route nodes of the given handler, resolves each argument node to its corresponding method
   * parameter, and parses the raw input token using the registered {@link ArgumentParser} for that parameter's type.
   *
   * @param handler the handler whose argument nodes are to be parsed.
   * @param args    the raw tokenized input.
   * @param source  the command source, forwarded to each parser.
   * @param target  the map to populate with resolved argument name → parsed value entries.
   * @throws ArgumentParseException if any argument token cannot be converted to its target type.
   */
  private void parseArguments(
    @NonNull RegisteredHandler handler,
    @NonNull String[] args,
    @NonNull CommandSource source,
    @NonNull Map<String, Object> target
  ) throws ArgumentParseException {
    var nodes = handler.nodes();
    var params = handler.method().getParameters();

    var argParamIndex = 1;
    for (var nodeIndex = 0; nodeIndex < nodes.size(); nodeIndex++) {
      var node = nodes.get(nodeIndex);
      if (node instanceof RouteNode.LiteralNode) {
        continue;
      }

      var argName = switch (node) {
        case RouteNode.RequiredArgNode required -> required.name();
        case RouteNode.OptionalArgNode optional -> optional.name();
        default -> throw new IllegalStateException("Unhandled node type: " + node);
      };

      if (nodeIndex >= args.length) {
        argParamIndex++;
        continue;
      }

      var rawToken = (node instanceof RouteNode.RequiredArgNode
        && argParamIndex == params.length - 1
        && params[argParamIndex].getType() == String.class)
        ? joinRemainingTokens(args, nodeIndex)
        : args[nodeIndex];

      if (argParamIndex < params.length) {
        var paramType = params[argParamIndex].getType();
        var parser = this.parserRegistry.require(paramType);
        target.put(argName, parser.parse(source, rawToken));
        argParamIndex++;
      }
    }
  }

  /**
   * Resolves the {@link ArgumentParser} for the method parameter at the given argument node index and appends its
   * tab-completion suggestions to the target list.
   *
   * @param handler the handler providing the method parameter metadata.
   * @param index   the index of the argument node within the handler's route.
   * @param source  the command source requesting completions.
   * @param partial the partial input typed so far.
   * @param target  the list to append suggestions to.
   */
  private void suggestFromParser(
    @NonNull RegisteredHandler handler,
    int index,
    @NonNull CommandSource source,
    @NonNull String partial,
    @NonNull List<String> target
  ) {
    var params = handler.method().getParameters();
    var argParamIndex = 1;

    for (var i = 0; i < index; i++) {
      if (!(handler.nodes().get(i) instanceof RouteNode.LiteralNode)) {
        argParamIndex++;
      }
    }

    if (argParamIndex >= params.length) {
      return;
    }

    var paramType = params[argParamIndex].getType();
    this.parserRegistry.find(paramType).ifPresent(parser -> {
      @SuppressWarnings("unchecked")
      var typeParser = (ArgumentParser<Object>) parser;
      target.addAll(typeParser.suggest(source, partial));
    });
  }

  /**
   * Joins all tokens from the given start index into a single space-delimited string.
   *
   * @param args the full token array.
   * @param from the index to start joining from (inclusive).
   * @return the joined string.
   */
  private @NonNull String joinRemainingTokens(@NonNull String @NonNull [] args, int from) {
    var builder = new StringBuilder();
    for (var i = from; i < args.length; i++) {
      if (i > from) {
        builder.append(' ');
      }
      builder.append(args[i]);
    }
    return builder.toString();
  }

  /**
   * Sends the auto-generated help listing to the given source, showing all handlers the source has permission to use.
   * Each entry is formatted as {@code /<root> <route>} followed by the handler's description, if present.
   *
   * @param source the command source to send the help listing to.
   */
  private void sendHelp(@NonNull CommandSource source) {
    source.sendMessage(Component.text("Available sub-commands:", NamedTextColor.GOLD));

    this.registry.handlers().stream()
      .filter(handler -> handler.permission() == null || source.hasPermission(handler.permission()))
      .forEach(handler -> {
        var usage = handler.buildUsage(this.root);
        var description = handler.description() != null ? " — " + handler.description() : "";

        source.sendMessage(Component.text(" " + usage, NamedTextColor.YELLOW)
          .append(Component.text(description, NamedTextColor.GRAY)));
      });
  }
}

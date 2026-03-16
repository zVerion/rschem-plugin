package me.verion.rschem.command.route;

import lombok.NonNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Converts a textual command-route pattern into an ordered, immutable list of {@link RouteNode}s.
 *
 * <p>The grammar recognized by this parser:
 * <pre>
 *   route          = segment (' ' segment)*
 *   segment        = literal | required_arg | optional_arg
 *   literal        = [a-zA-Z0-9_-]+
 *   required_arg   = '<' name '>'
 *   optional_arg   = '[' name ']'
 *   name           = [a-zA-Z0-9_-]+
 * </pre>
 *
 * <p>Structural constraints enforced at parse time:
 * <ul>
 *   <li>Optional nodes must not precede required nodes.
 *   <li>Literal nodes must not appear after any argument node.
 *   <li>Empty patterns are not accepted.
 * </ul>
 *
 * @since 2.0
 */
public final class RouteParser {

  /** The pattern used to validate literal tokens and argument names. */
  public static final Pattern NAME_PATTERN = Pattern.compile("[a-zA-Z0-9_-]+");

  private RouteParser() {
    throw new UnsupportedOperationException();
  }

  /**
   * Parses the given pattern into an immutable, ordered list of {@link RouteNode nodes}.
   *
   * @param pattern the route pattern to parse (e.g. {@code "config set <key> [value]"}).
   * @return an unmodifiable list of route nodes in declaration order.
   * @throws IllegalArgumentException if the pattern is blank or violates any structural constraint.
   * @throws NullPointerException     if the given pattern is null.
   */
  public static @NonNull @UnmodifiableView List<RouteNode> parse(@NonNull String pattern) {
    if (pattern.isBlank()) {
      throw new IllegalArgumentException("Route pattern must not be blank");
    }

    var tokens = pattern.strip().split("\\s+");
    var nodes = new ArrayList<RouteNode>(tokens.length);

    var seenArgNode = false;
    var seenOptionalNode = false;

    for (var token : tokens) {
      var node = parseToken(token);
      if (node instanceof RouteNode.LiteralNode && seenArgNode) {
        throw new IllegalArgumentException(
          "Literal node '" + token + "' may not appear after an argument node in pattern: '" + pattern + "'");
      }

      if (node instanceof RouteNode.RequiredArgNode && seenOptionalNode) {
        throw new IllegalArgumentException(
          "Required argument node '" + token + "' may not appear after an optional node in pattern: '" + pattern + "'");
      }

      if (node instanceof RouteNode.RequiredArgNode || node instanceof RouteNode.OptionalArgNode) {
        seenArgNode = true;
      }

      if (node instanceof RouteNode.OptionalArgNode) {
        seenOptionalNode = true;
      }
      nodes.add(node);
    }
    return Collections.unmodifiableList(nodes);
  }

  /**
   * Parses a single token into a {@link RouteNode}.
   *
   * @param token the raw token to parse.
   * @return the corresponding route node.
   * @throws IllegalArgumentException if the token is malformed or contains an invalid name.
   */
  private static @NonNull RouteNode parseToken(@NonNull String token) {
    if (token.startsWith("<") && token.endsWith(">")) {
      var name = token.substring(1, token.length() - 1);
      validateName(name, token);
      return new RouteNode.RequiredArgNode(name);
    }

    if (token.startsWith("[") && token.endsWith("]")) {
      var name = token.substring(1, token.length() - 1);
      validateName(name, token);
      return new RouteNode.OptionalArgNode(name);
    }

    if (token.isEmpty() || !NAME_PATTERN.matcher(token).matches()) {
      throw new IllegalArgumentException("Invalid literal token: '" + token + "'");
    }

    return new RouteNode.LiteralNode(token);
  }

  /**
   * Validates that the given argument name is non-empty and matches {@link #NAME_PATTERN}.
   *
   * @param name     the extracted argument name to validate.
   * @param original the full original token, used for error reporting.
   * @throws IllegalArgumentException if the name is empty or does not match the allowed pattern.
   */
  private static void validateName(@NonNull String name, @NonNull String original) {
    if (name.isEmpty() || !NAME_PATTERN.matcher(name).matches()) {
      throw new IllegalArgumentException("Invalid argument name in token '" + original + "'");
    }
  }
}

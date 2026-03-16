package me.verion.rschem.command.route;

import lombok.NonNull;

/**
 * Represents a single segment of a parsed command route.
 * <p>
 * Routes are decomposed into sequences of {@code RouteNode}s at registration time by the {@link RouteParser}. There are
 * three distinct node kinds, modeled as a sealed hierarchy:
 *
 * <ul>
 *   <li>{@link LiteralNode}     — a fixed keyword that must match exactly.
 *   <li>{@link RequiredArgNode} — a required variable ({@code <name>}).
 *   <li>{@link OptionalArgNode} — an optional variable ({@code [name]}).
 * </ul>
 *
 * @since 2.0
 */
public sealed interface RouteNode permits RouteNode.LiteralNode, RouteNode.RequiredArgNode, RouteNode.OptionalArgNode {

  /**
   * Returns the display token for this node as it appears in a usage string.
   *
   * @return the display token.
   */
  @NonNull String token();

  /**
   * Returns whether this node matches the given input token at dispatch-time.
   *
   * @param input the raw token from the user's input.
   * @return {@code true} if this node accepts the token, {@code false} otherwise.
   * @throws NullPointerException if the given input is null.
   */
  boolean matches(@NonNull String input);

  /**
   * A fixed keyword node that performs a case-insensitive exact match against its token.
   *
   * @param token the literal keyword to match.
   * @since 2.0
   */
  record LiteralNode(@NonNull String token) implements RouteNode {

    @Override
    public boolean matches(@NonNull String input) {
      return this.token.equalsIgnoreCase(input);
    }
  }

  /**
   * A required argument node that accepts any non-empty token and captures it under {@link #name()}.
   *
   * @param name the argument name, without surrounding angle brackets.
   * @since 2.0
   */
  record RequiredArgNode(@NonNull String name) implements RouteNode {

    @Override
    public @NonNull String token() {
      return "<" + this.name + ">";
    }

    @Override
    public boolean matches(@NonNull String input) {
      return !input.isBlank();
    }
  }

  /**
   * An optional argument node that accepts any non-empty token or the absence of a token entirely.
   * Optional nodes must always appear after all required and literal nodes in a route.
   *
   * @param name the argument name, without surrounding square brackets.
   * @since 2.0
   */
  record OptionalArgNode(@NonNull String name) implements RouteNode {

    @Override
    public @NonNull String token() {
      return "[" + this.name + "]";
    }

    @Override
    public boolean matches(@NonNull String input) {
      return true;
    }
  }
}

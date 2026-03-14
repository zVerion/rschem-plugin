package me.verion.rschem.util;

import lombok.NonNull;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

/**
 * Utility class for constructing and sending consistently styled chat messages to {@link CommandSender CommandSenders}.
 *
 * <p>Every message is prefixed with the plugin prefix and an optional icon that reflects the semantic intent of the
 * message. Four named intent levels are provided:
 * <ul>
 *   <li>{@link #ok()} — a successful operation (green {@code ✔})</li>
 *   <li>{@link #warn()} — a non-fatal warning (yellow {@code ⚠})</li>
 *   <li>{@link #error()} — a user-facing error (red {@code ✘})</li>
 *   <li>{@link #info()} — neutral information (gray {@code »})</li>
 * </ul>
 *
 * <p>Each intent level exposes both a one-shot convenience overload that immediately sends a plain-text message, and a
 * {@link Builder} factory method for constructing richer messages with inline highlights, values, or clickable commands.
 *
 * <p>This class is a pure utility class and cannot be instantiated.
 *
 * @since 2.0
 */
public final class MessageUtil {

  /**
   * The shared plugin prefix prepended to every message produced by this class.
   *
   * <p>Rendered as {@code [Rschem] } with the bracket pair in dark-gray and the plugin name
   * in bold gold.
   */
  static final Component PREFIX = Component.text()
    .append(Component.text("[", NamedTextColor.DARK_GRAY))
    .append(Component.text("RSchem", NamedTextColor.GOLD, TextDecoration.BOLD))
    .append(Component.text("] ", NamedTextColor.DARK_GRAY))
    .build();

  private static final Component ICON_OK = Component.text("✔ ", NamedTextColor.GREEN);
  private static final Component ICON_WARN = Component.text("⚠ ", NamedTextColor.YELLOW);
  private static final Component ICON_ERROR = Component.text("✘ ", NamedTextColor.RED);
  private static final Component ICON_INFO = Component.text("» ", NamedTextColor.GRAY);

  private MessageUtil() {
    throw new UnsupportedOperationException();
  }

  /**
   * Returns a new {@link Builder} configured for a success message, prefixed with a green {@code ✔} icon.
   *
   * @return a new {@link Builder} for an ok-level message
   */
  @Contract("-> new")
  public static @NonNull Builder ok() {
    return new Builder(ICON_OK, NamedTextColor.GRAY);
  }

  /**
   * Sends a plain-text success message to the given sender.
   *
   * @param sender  the recipient of the message
   * @param message the plain-text message body
   */
  public static void ok(@NonNull CommandSender sender, @NonNull String message) {
    ok().text(message).send(sender);
  }

  /**
   * Returns a new {@link Builder} configured for a warning message, prefixed with a yellow {@code ⚠} icon.
   *
   * @return a new {@link Builder} for a warn-level message
   */
  @Contract("-> new")
  public static @NonNull Builder warn() {
    return new Builder(ICON_WARN, NamedTextColor.YELLOW);
  }

  /**
   * Sends a plain-text warning message to the given sender.
   *
   * @param sender  the recipient of the message
   * @param message the plain-text message body
   */
  public static void warn(@NonNull CommandSender sender, @NonNull String message) {
    warn().text(message).send(sender);
  }

  /**
   * Returns a new {@link Builder} configured for an error message, prefixed with a red {@code ✘} icon.
   *
   * @return a new {@link Builder} for an error-level message
   */
  @Contract("-> new")
  public static @NonNull Builder error() {
    return new Builder(ICON_ERROR, NamedTextColor.RED);
  }

  /**
   * Sends a plain-text error message to the given sender.
   *
   * @param sender  the recipient of the message
   * @param message the plain-text message body
   */
  public static void error(@NonNull CommandSender sender, @NonNull String message) {
    error().text(message).send(sender);
  }

  /**
   * Returns a new {@link Builder} configured for an informational message, prefixed with a gray {@code »} icon.
   *
   * @return a new {@link Builder} for an info-level message
   */
  @Contract("-> new")
  public static @NonNull Builder info() {
    return new Builder(ICON_INFO, NamedTextColor.GRAY);
  }

  /**
   * Sends a plain-text informational message to the given sender.
   *
   * @param sender  the recipient of the message
   * @param message the plain-text message body
   */
  public static void info(@NonNull CommandSender sender, @NonNull String message) {
    info().text(message).send(sender);
  }

  /**
   * Returns a new {@link Builder} with no icon, for constructing messages that only carry the plugin prefix.
   *
   * @return a new icon-less {@link Builder}
   */
  @Contract("-> new")
  public static @NonNull Builder plain() {
    return new Builder(null, NamedTextColor.GRAY);
  }

  /**
   * A fluent builder for constructing richly formatted plugin messages.
   *
   * <p>Every builder instance is pre-populated with the plugin {@link MessageUtil#PREFIX} and an optional intent icon.
   * Text segments can be appended via the various {@code text}, {@code highlight}, {@code value}, {@code component},
   * and {@code command} methods, each of which returns {@code this} to enable method chaining.
   *
   * <p>Call {@link #build()} to obtain the finished {@link Component}, or {@link #send(CommandSender)} to build and
   * dispatch it in one step.
   *
   * <p>Builder instances are not thread-safe and must not be shared across threads.
   *
   * @since 1.0
   */
  public static final class Builder {

    private final TextComponent.Builder root;
    private final NamedTextColor bodyColor;

    /**
     * Creates a new builder with the given icon and body color.
     *
     * @param icon      an optional leading icon component; {@code null} produces no icon
     * @param bodyColor the default colour applied to text segments added via {@link #text(String)}
     */
    private Builder(@Nullable Component icon, @NonNull NamedTextColor bodyColor) {
      this.bodyColor = bodyColor;
      this.root = Component.text().append(PREFIX);
      if (icon != null) this.root.append(icon);
    }

    /**
     * Appends a plain-text segment using the builder's default body colour.
     *
     * @param text the text to append
     * @return the same instance as used to call the method, for chaining.
     */
    @Contract("_ -> this")
    public @NonNull Builder text(@NonNull String text) {
      this.root.append(Component.text(text, this.bodyColor));
      return this;
    }

    /**
     * Appends a highlighted text segment in aqua, typically used for names or keywords that
     * should stand out from the surrounding body text.
     *
     * @param text the text to append
     * @return the same instance as used to call the method, for chaining.
     */
    @Contract("_ -> this")
    public @NonNull Builder highlight(@NonNull String text) {
      this.root.append(Component.text(text, NamedTextColor.AQUA));
      return this;
    }

    /**
     * Appends a value segment in yellow, typically used for numbers, coordinates, or other
     * dynamic values embedded within a message.
     *
     * @param text the text to append
     * @return the same instance as used to call the method, for chaining.
     */
    @Contract("_ -> this")
    public @NonNull Builder value(@NonNull String text) {
      this.root.append(Component.text(text, NamedTextColor.YELLOW));
      return this;
    }

    /**
     * Appends an arbitrary pre-built {@link Component} to this message.
     *
     * @param component the component to append
     * @return the same instance as used to call the method, for chaining.
     */
    @Contract("_ -> this")
    public @NonNull Builder component(@NonNull Component component) {
      this.root.append(component);
      return this;
    }

    /**
     * Appends a clickable, underlined aqua command suggestion segment.
     *
     * <p>Clicking the segment in chat fills the player's input bar with the given command via a
     * {@link ClickEvent#suggestCommand(String)} event. If {@code hoverHint} is non-null, hovering over the segment
     * displays it as a tooltip.
     *
     * @param command   the command string inserted into the chat input on click
     * @param hoverHint an optional tooltip shown when hovering over the segment, or
     *                  {@code null} for no tooltip
     * @return the same instance as used to call the method, for chaining.
     */
    @Contract("_, _ -> this")
    public @NonNull Builder command(@NonNull String command, @Nullable String hoverHint) {
      var builder = Component.text()
        .content(command)
        .color(NamedTextColor.AQUA)
        .decorate(TextDecoration.UNDERLINED)
        .clickEvent(ClickEvent.suggestCommand(command));

      if (hoverHint != null) {
        builder.hoverEvent(HoverEvent.showText(Component.text(hoverHint, NamedTextColor.GRAY)));
      }

      this.root.append(builder.build());
      return this;
    }

    /**
     * Builds and returns the fully assembled {@link Component}.
     *
     * @return the finished message component
     */
    @Contract("-> new")
    public @NonNull Component build() {
      return this.root.build();
    }

    /**
     * Builds the message and sends it to the given {@link CommandSender}.
     *
     * <p>Shorthand for {@code sender.sendMessage(this.build())}.
     *
     * @param sender the recipient of the message
     */
    public void send(@NonNull CommandSender sender) {
      sender.sendMessage(this.build());
    }
  }
}

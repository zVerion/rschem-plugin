package me.verion.rschem.command.context;

import lombok.NonNull;
import me.verion.rschem.common.language.I18n;
import me.verion.rschem.common.language.Placeholder;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Represents the source of a command execution, wrapping a Bukkit {@link CommandSender} and providing a unified API for
 * sending localized messages and checking permissions.
 * <p>
 * Two implementations are provided: {@link PlayerSource} for in-game players and {@link ConsoleSource} for the server
 * console or any other non-player sender.
 *
 * @since 2.0
 */
public sealed interface CommandSource permits CommandSource.PlayerSource, CommandSource.ConsoleSource {

  /**
   * Creates a new {@link CommandSource} wrapping the given {@link CommandSender}.
   *
   * @param sender the sender to wrap.
   * @return a {@link PlayerSource} if {@code sender} is a {@link Player}, otherwise a {@link ConsoleSource}.
   * @throws NullPointerException if the given sender is null.
   */
  static @NonNull CommandSource of(@NonNull CommandSender sender) {
    return sender instanceof Player player ? new PlayerSource(player) : new ConsoleSource(sender);
  }

  /**
   * Returns the underlying Bukkit {@link CommandSender}.
   *
   * @return the wrapped sender.
   */
  @NonNull
  CommandSender sender();

  /**
   * Sends a localized message resolved by the given translation key to the wrapped sender, applying the given
   * placeholders before delivery.
   *
   * @param key          the translation key to resolve.
   * @param placeholders the placeholders to apply to the resolved message.
   * @throws NullPointerException if the given key or placeholders are null.
   */
  void sendMessage(@NonNull String key, @NonNull Placeholder @NonNull ... placeholders);

  /**
   * Returns whether the wrapped sender holds the given permission node.
   *
   * @param permission the permission node to check.
   * @return {@code true} if the sender has the permission, {@code false} otherwise.
   * @throws NullPointerException if the given permission is null.
   */
  default boolean hasPermission(@NonNull String permission) {
    return this.sender().hasPermission(permission);
  }

  /**
   * Sends a {@link Component} message to the wrapped sender.
   *
   * @param message the message to deliver.
   * @throws NullPointerException if the given message is null.
   */
  default void sendMessage(@NonNull Component message) {
    this.sender().sendMessage(message);
  }

  /**
   * Returns whether this source originates from an in-game {@link Player}.
   *
   * @return {@code true} if this is a {@link PlayerSource}, {@code false} otherwise.
   */
  default boolean isPlayer() {
    return this instanceof PlayerSource;
  }

  /**
   * Returns whether this source originates from the server console or any non-player sender.
   *
   * @return {@code true} if this is a {@link ConsoleSource}, {@code false} otherwise.
   */
  default boolean isConsole() {
    return this instanceof ConsoleSource;
  }

  /**
   * A {@link CommandSource} originating from an in-game {@link Player}.
   *
   * @param player the backing Bukkit player.
   * @since 2.0
   */
  record PlayerSource(@NonNull Player player) implements CommandSource {

    @Override
    public @NonNull CommandSender sender() {
      return this.player;
    }

    @Override
    public void sendMessage(@NonNull String key, @NonNull Placeholder @NonNull ... placeholders) {
      this.player.sendMessage(I18n.get(this.player, key, placeholders));
    }
  }

  /**
   * A {@link CommandSource} originating from the server console or any non-player sender.
   *
   * @param sender the backing Bukkit sender.
   * @since 2.0
   */
  record ConsoleSource(@NonNull CommandSender sender) implements CommandSource {

    @Override
    public void sendMessage(@NonNull String key, @NonNull Placeholder @NonNull ... placeholders) {
      this.sender.sendMessage(I18n.get(I18n.defaultLocale(), key, placeholders));
    }
  }
}

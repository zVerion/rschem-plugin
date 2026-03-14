package me.verion.rschem.command;

import lombok.NonNull;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface SubCommand {

  /**
   * Returns the name of this sub-command as it appears in the command input.
   *
   * @return the sub-command name (e.g. {@code "copy"}, {@code "paste"})
   */
  @Contract(pure = true)
  @NonNull
  String name();

  /**
   * Returns the permission node required to execute this sub-command, or {@code null} if no permission check is
   * performed.
   *
   * @return the required permission node, or {@code null} if none is required
   */
  @Contract(pure = true)
  @Nullable String permission();

  /**
   * Returns a short human-readable description of what this sub-command does.
   *
   * @return the description of this sub-command
   */
  @Contract(pure = true)
  @NonNull
  String description();

  /**
   * Returns the usage string for this sub-command, shown when the sender provides invalid arguments.
   *
   * @return the usage string for this sub-command
   */
  @Contract(pure = true)
  @NonNull
  String usage();

  /**
   * Returns whether this sub-command requires the sender to be an online {@link Player}.
   *
   * @return {@code true} if only players may execute this sub-command, {@code false} if any {@link CommandSender} is
   * accepted
   */
  @Contract(pure = true)
  default boolean requiresPlayer() {
    return true;
  }

  /**
   * Executes this sub-command for the given sender with the provided arguments.
   * <p>
   * The root executor guarantees that the sender has the required permission and, if {@link #requiresPlayer()} returns
   * {@code true}, that the sender is an online {@link Player} before this method is called. {@code args} contains only
   * the arguments following the sub-command name; the sub-command name itself is not included.
   *
   * @param sender the sender who invoked the sub-command
   * @param args   the arguments supplied after the sub-command name, may be empty
   */
  void execute(@NonNull CommandSender sender, @NonNull String[] args);

  /**
   * Returns a list of tab-completion suggestions for the current input state.
   * <p>
   * The default implementation returns an empty list, producing no suggestions. Override to provide context-aware
   * completions based on {@code args}. {@code args} contains only the arguments following the sub-command name; the
   * sub-command name itself is not included.
   *
   * @param sender the sender requesting tab completions
   * @param args   the arguments supplied so far after the sub-command name, may be empty
   * @return a list of completion suggestions for the current input; never {@code null}
   */
  @Contract(pure = true)
  @NonNull
  default List<String> tabComplete(@NonNull CommandSender sender, @NonNull String[] args) {
    return List.of();
  }
}

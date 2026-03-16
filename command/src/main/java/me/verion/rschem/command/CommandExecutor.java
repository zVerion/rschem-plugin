package me.verion.rschem.command;

import com.google.common.base.Preconditions;
import lombok.NonNull;
import me.verion.rschem.command.annotation.SubCommand;
import me.verion.rschem.command.context.CommandSource;
import me.verion.rschem.command.parser.ArgumentParser;
import me.verion.rschem.command.registry.ArgumentParserRegistry;
import me.verion.rschem.command.registry.CommandRegistry;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * A Bukkit {@link TabExecutor} that bridges incoming command and tab-completion events to the framework's
 * {@link CommandDispatcher}.
 *
 * @since 2.0
 */
public final class CommandExecutor implements TabExecutor {

  private final CommandDispatcher dispatcher;

  /**
   * Creates a new {@link CommandExecutor} backed by the given {@link CommandDispatcher}.
   *
   * @param dispatcher the dispatcher to forward all command and tab-completion events to.
   * @throws NullPointerException if the given dispatcher is null.
   */
  public CommandExecutor(@NonNull CommandDispatcher dispatcher) {
    this.dispatcher = dispatcher;
  }

  /**
   * Creates a new command executor with the given name.
   *
   * @param root the command executor to create
   * @return the new command executor.
   * @throws NullPointerException if the given permission is null.
   */
  public static @NonNull CommandExecutor of(@NonNull String root) {
    return builder().root(root).build();
  }

  /**
   * Creates a new command executor builder instance.
   *
   * @return the new builder instance.
   */
  public static @NonNull Builder builder() {
    return new Builder();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean onCommand(
    @NonNull CommandSender sender,
    @NonNull Command command,
    @NonNull String label,
    @NonNull String @NonNull [] args
  ) {
    this.dispatcher.dispatch(CommandSource.of(sender), args);
    return true;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @Nullable List<String> onTabComplete(
    @NonNull CommandSender sender,
    @NonNull Command command,
    @NonNull String label,
    @NonNull String @NonNull [] args
  ) {
    return this.dispatcher.complete(CommandSource.of(sender), args);
  }

  /**
   * A builder for a {@link CommandExecutor}.
   *
   * @since 4.0
   */
  public static final class Builder {

    private final CommandRegistry registry = new CommandRegistry();
    private final ArgumentParserRegistry parserRegistry = new ArgumentParserRegistry();

    private String root;

    /**
     * Sets the root this command executor.
     *
     * @param root the root of the command executor.
     * @return the same instance as used to call the method, for chaining.
     */
    public @NonNull Builder root(@NonNull String root) {
      this.root = root;
      return this;
    }

    /**
     * Scans the given handler instance for {@link SubCommand} annotated methods and registers them.
     *
     * @param handler the handler instance to scan.
     * @return the same instance as used to call the method, for chaining.
     * @throws NullPointerException if the given handler is null.
     */
    public @NonNull Builder register(@NonNull Object handler) {
      this.registry.register(handler);
      return this;
    }

    /**
     * Registers a custom {@link ArgumentParser} for the given type, overriding any previously registered parser
     * including the defaults.
     *
     * @param type   the target type to register the parser for.
     * @param parser the parser to register.
     * @param <T>    the parsed value type.
     * @return the same instance as used to call the method, for chaining.
     * @throws NullPointerException if the given type or parser is null.
     */
    public <T> @NonNull Builder parser(@NonNull Class<T> type, @NonNull ArgumentParser<T> parser) {
      this.parserRegistry.register(type, parser);
      return this;
    }

    /**
     * Builds the new command executor with all previously set options.
     *
     * @return the new command executor.
     * @throws NullPointerException if the root of the command executor is missing.
     */
    public @NonNull CommandExecutor build() {
      Preconditions.checkNotNull(this.root, "No root given");

      return new CommandExecutor(new CommandDispatcher(this.registry, this.parserRegistry, this.root));
    }
  }
}

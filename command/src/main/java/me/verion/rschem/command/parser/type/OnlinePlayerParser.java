package me.verion.rschem.command.parser.type;

import me.verion.rschem.command.context.CommandSource;
import me.verion.rschem.command.exception.ArgumentParseException;
import me.verion.rschem.command.parser.ArgumentParser;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NonNull;

import java.util.List;


/**
 * An {@link ArgumentParser} that resolves a raw string token into an online {@link Player} by exact name.
 * <p>
 * Tab-completion suggestions are filtered from the currently online player list using a case-insensitive prefix match
 * against the partial input.
 *
 * @since 2.0
 */
final class OnlinePlayerParser implements ArgumentParser<Player> {

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull Player parse(@NonNull CommandSource source, @NonNull String input) throws ArgumentParseException {
    var player = Bukkit.getPlayerExact(input);
    if (player == null) {
      throw new ArgumentParseException("No online player named '" + input + "' was found");
    }

    return player;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull @Unmodifiable List<String> suggest(@NonNull CommandSource source, @NonNull String partialInput) {
    var lowerPartial = partialInput.toLowerCase();
    return Bukkit.getOnlinePlayers().stream()
      .map(Player::getName)
      .filter(name -> name.toLowerCase().startsWith(lowerPartial))
      .toList();
  }
}

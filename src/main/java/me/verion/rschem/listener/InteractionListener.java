package me.verion.rschem.listener;

import lombok.NonNull;
import me.verion.rschem.RSchemPlugin;
import me.verion.rschem.selection.Selection;
import me.verion.rschem.session.EditSession;
import me.verion.rschem.util.BlockVector3;
import me.verion.rschem.util.MessageUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.Contract;

/**
 * Bukkit event listener that handles player session lifecycle and wand interactions.
 * <p>
 * Sessions are created eagerly when a player joins and disposed of when they leave, ensuring that no state leaks
 * between sessions. Wand interactions set the first or second corner of the player's active {@link Selection} and send
 * a confirmation message with the new position and, if the selection is complete, the resulting block volume.
 *
 * @since 2.0
 */
public record InteractionListener(@NonNull RSchemPlugin pluginMainClass) implements Listener {

  /**
   * Creates a new session for the joining player.
   *
   * @param event the join event
   */
  @EventHandler
  public void onJoin(@NonNull PlayerJoinEvent event) {
    this.pluginMainClass.sessions().getOrCreate(event.getPlayer());
  }

  /**
   * Disposes of the session of the leaving player.
   *
   * @param event the quit event
   */
  @EventHandler
  public void onQuit(@NonNull PlayerQuitEvent event) {
    this.pluginMainClass.sessions().invalidate(event.getPlayer().getUniqueId());
  }

  /**
   * Handles wand left- and right-click interactions, setting the first or second selection
   * corner respectively.
   * <p>
   * The interaction is consumed regardless of game mode to prevent block damage or placement side effects when the wand
   * is used in creative mode.
   *
   * @param event the interact event
   */
  @EventHandler
  public void onInteract(@NonNull PlayerInteractEvent event) {
    if (event.getHand() != EquipmentSlot.HAND) return;

    var action = event.getAction();
    if (action != Action.LEFT_CLICK_BLOCK && action != Action.RIGHT_CLICK_BLOCK) return;
    if (event.getClickedBlock() == null) return;

    var player = event.getPlayer();
    var configuration = this.pluginMainClass.configuration();

    if (player.getInventory().getItemInMainHand().getType() != configuration.wandMaterial()) return;
    if (!player.hasPermission("rschem.use")) return;

    event.setCancelled(true);

    var session = this.pluginMainClass.sessions().getOrCreate(player);
    var clicked = BlockVector3.fromLocation(event.getClickedBlock().getLocation());

    if (action == Action.LEFT_CLICK_BLOCK) {
      session.pos1(clicked);
      this.sendPositionMessage(player, session, 1, clicked);
    } else {
      session.pos2(clicked);
      this.sendPositionMessage(player, session, 2, clicked);
    }
  }

  /**
   * Sends a position-set confirmation message to the given player, including the block
   * volume of the current selection if it is complete.
   *
   * @param recipient the recipient
   * @param session   the player's session, used to read the current selection volume
   * @param corner    the corner number that was just set ({@code 1} or {@code 2})
   * @param pos       the position that was set
   */
  private void sendPositionMessage(
    @NonNull Player recipient,
    @NonNull EditSession session,
    int corner,
    @NonNull BlockVector3 pos
  ) {
    var builder = MessageUtil.ok()
      .text("Position ")
      .value(String.valueOf(corner))
      .text(" set to ")
      .highlight(this.formatPosition(pos));

    long volume = session.selection().volume();
    if (volume > 0) {
      builder.text(" | ")
        .value(String.format("%,d", volume))
        .text(" blocks");
    }

    builder.send(recipient);
  }

  /**
   * Formats the given position as a human-readable coordinate string.
   *
   * @param pos the position to format
   * @return a string of the form {@code "x, y, z"}
   */
  @Contract(pure = true)
  private @NonNull String formatPosition(@NonNull BlockVector3 pos) {
    return pos.x() + ", " + pos.y() + ", " + pos.z();
  }
}

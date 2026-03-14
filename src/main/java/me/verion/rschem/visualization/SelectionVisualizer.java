package me.verion.rschem.visualization;

import lombok.NonNull;
import me.verion.rschem.selection.Selection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;

/**
 * Renders a visual overlay for a player's active {@link Selection} in the game world.
 * <p>
 * Implementations are responsible for both displaying and cleaning up any client-side effects (such as particles or
 * block highlights) associated with a selection. When a selection changes or is cleared, {@link #clear(Player)} should
 * always be called before issuing a new {@link #render(Player, Selection)} call to avoid stale visual artifacts.
 * <p>
 * Each implementation must expose a unique {@link #type()} identifier so that multiple visualizer implementations can
 * coexist and be selected by name at runtime.
 *
 * @since 2.0
 */
public interface SelectionVisualizer {

  /**
   * Renders a visual overlay of the given selection for the specified player.
   * <p>
   * Calling this method while a previous overlay is still active may produce overlapping or duplicate effects. Callers
   * should invoke {@link #clear(Player)} first to ensure a clean visual state.
   *
   * @param player    the player for whom the overlay is rendered
   * @param selection the selection to visualize; must be complete if region bounds are required
   */
  void render(@NonNull Player player, @NonNull Selection selection);

  /**
   * Removes any active visual overlay previously rendered for the given player.
   * <p>
   * This method is a no-op if no overlay is currently displayed for the player.
   *
   * @param player the player whose overlay should be removed
   */
  void clear(@NonNull Player player);

  /**
   * Returns the unique type identifier of this visualizer implementation.
   * <p>
   * The returned string is used to look up and select a specific visualizer by name at runtime. Implementations must
   * ensure that this value is unique across all registered visualizers.
   *
   * @return the non-empty type identifier of this visualizer
   */
  @Contract(pure = true)
  @NonNull
  String type();
}

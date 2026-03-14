package me.verion.rschem.session;

import lombok.NonNull;
import me.verion.rschem.internal.session.SessionManagerImpl;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/**
 * Manages the lifecycle of per-player {@link EditSession} instances.
 * <p>
 * Session are created on demand and disposed of either individually via {@link #invalidate(UUID)} or collectively via
 * {@link #invalidateAll()}. The latter should be called during plugin shutdown to release all resources held by active
 * sessions.
 * <p>
 * Use {@link #newSession()} to obtain a new implementation instance. The plugin is responsible for holding the
 * reference and passing it to consumers — no global registry is provided intentionally.
 *
 * @since 2.0
 */
public interface SessionManager {

  /**
   * Creates and returns a new default {@link SessionManager} implementation.
   *
   * @return a new {@link SessionManager} instance
   */
  @Contract("-> new")
  static @NonNull SessionManager newSession() {
    return new SessionManagerImpl(50);
  }

  /**
   * Creates and returns a new default {@link SessionManager} implementation given the history size accessible.
   *
   * @param maxHistorySize the maximum undo/redo history depth per session
   * @return a new {@link SessionManager} instance
   */
  @Contract("_, -> new")
  static @NonNull SessionManager newSession(int maxHistorySize) {
    return new SessionManagerImpl(maxHistorySize);
  }

  /**
   * Returns the {@link EditSession} for the given player, creating a new one if none exists yet.
   *
   * @param player the player whose session is requested
   * @return the existing or newly created {@link EditSession} for the player
   */
  @NonNull
  EditSession getOrCreate(@NonNull Player player);

  /**
   * Returns the {@link EditSession} associated with the given player UUID, if one exists.
   *
   * @param uniqueId the UUID of the player to look up
   * @return an {@link Optional} containing the session, or {@link Optional#empty()} if no session has been created for
   * the given player
   */
  @NonNull
  Optional<EditSession> find(@NonNull UUID uniqueId);

  /**
   * Returns a read-only view of all currently active sessions. The returned collection reflects the state of the
   * manager at the time of the call. Mutations to the manager after this call may or may not be visible in the returned
   * collection, depending on the implementation.
   *
   * @return all currently active {@link EditSession} instances
   */
  @NonNull
  @Unmodifiable
  Collection<EditSession> sessions();

  /**
   * Removes and disposes of the session associated with the given player UUID, if one exists.
   *
   * @param uniqueId the UUID of the player whose session should be invalidated
   */
  void invalidate(@NonNull UUID uniqueId);

  /**
   * Removes and disposes of all currently active sessions. This method should be called during plugin shutdown to
   * ensure that all resources held by active sessions are released cleanly. After this call, {@link #sessions()}
   * returns an empty collection.
   */
  void invalidateAll();
}

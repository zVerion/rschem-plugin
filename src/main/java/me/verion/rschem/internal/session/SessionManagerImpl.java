package me.verion.rschem.internal.session;

import lombok.NonNull;
import me.verion.rschem.session.EditSession;
import me.verion.rschem.session.HistoryEntry;
import me.verion.rschem.session.SessionManager;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default {@link SessionManager} implementation backed by a {@link ConcurrentHashMap} for safe concurrent read access
 * s across threads.
 * <p>
 * Sessions are created lazily on first access via {@link #getOrCreate(Player)} and disposed of either individually
 * through {@link #invalidate(UUID)} or collectively through {@link #invalidateAll()}, which should be called during
 * plugin shutdown.
 * <p>
 * While the session map itself is thread-safe, individual {@link EditSession} instances are not. Callers must ensure
 * that session operations are performed on the server's main thread.
 *
 * @since 2.0
 */
public final class SessionManagerImpl implements SessionManager {

  private final Map<UUID, EditSessionImpl> sessions = new ConcurrentHashMap<>();
  private final int maxHistorySize;

  /**
   * Creates a new {@code SessionManagerImpl} with the given undo history limit.
   *
   * @param maxHistorySize the maximum number of {@link HistoryEntry} instances retained per session before the oldest
   *                       entries are evicted
   */
  public SessionManagerImpl(int maxHistorySize) {
    this.maxHistorySize = maxHistorySize;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull EditSession getOrCreate(@NonNull Player player) {
    return this.sessions.computeIfAbsent(
      player.getUniqueId(),
      uniqueId -> new EditSessionImpl(uniqueId, this.maxHistorySize)
    );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull Optional<EditSession> find(@NonNull UUID uniqueId) {
    return Optional.ofNullable(this.sessions.get(uniqueId));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull @Unmodifiable Collection<EditSession> sessions() {
    return Collections.unmodifiableCollection(this.sessions.values());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void invalidate(@NonNull UUID uniqueId) {
    var session = this.sessions.remove(uniqueId);
    if (session != null) {
      session.dispose();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void invalidateAll() {
    this.sessions.values().forEach(EditSessionImpl::dispose);
    this.sessions.clear();
  }

  /**
   * Returns the number of sessions currently held by this manager.
   *
   * @return the current session count, always {@code >= 0}
   */
  @Contract(pure = true)
  public int sessionCount() {
    return this.sessions.size();
  }
}

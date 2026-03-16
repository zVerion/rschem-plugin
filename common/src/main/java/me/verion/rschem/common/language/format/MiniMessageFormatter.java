package me.verion.rschem.common.language.format;

import lombok.NonNull;
import me.verion.rschem.common.language.Placeholder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A {@link MessageFormatter} implementation backed by the Adventure {@link MiniMessage} parser.
 * <p>
 * Messages containing no {@link Placeholder}s are cached in a {@link ConcurrentHashMap} to avoid redundant
 * deserialization on repeated invocations. The cache can be cleared via {@link #clearCache()}.
 *
 * @since 2.0
 */
public final class MiniMessageFormatter implements MessageFormatter {

  private final MiniMessage miniMessage = MiniMessage.miniMessage();
  private final Map<String, Component> noPlaceholderCache = new ConcurrentHashMap<>();

  /**
   * {@inheritDoc}
   */
  @Override
  public @NonNull Component format(@NonNull String raw, @NonNull Placeholder @NonNull ... placeholders) {
    if (placeholders.length == 0) {
      return this.noPlaceholderCache.computeIfAbsent(raw, this.miniMessage::deserialize);
    }

    return this.miniMessage.deserialize(raw, this.buildTagResolver(placeholders));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void clearCache() {
    this.noPlaceholderCache.clear();
  }

  /**
   * Returns the current number of entries held in the no-placeholder cache.
   *
   * @return the cache size.
   */
  public int cacheSize() {
    return this.noPlaceholderCache.size();
  }

  /**
   * Builds a combined {@link TagResolver} from the given {@link Placeholder} array, mapping each placeholder's
   * key to its string value as an unparsed tag.
   *
   * @param placeholders the placeholders to convert into tag resolvers.
   * @return a combined {@link TagResolver} covering all given placeholders.
   */
  private @NonNull TagResolver buildTagResolver(@NonNull Placeholder @NonNull [] placeholders) {
    var resolvers = new TagResolver[placeholders.length];
    for (var i = 0; i < placeholders.length; i++) {
      var placeholder = placeholders[i];
      resolvers[i] = net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.unparsed(
        placeholder.key(),
        placeholder.value().toString()
      );
    }
    return TagResolver.resolver(resolvers);
  }
}

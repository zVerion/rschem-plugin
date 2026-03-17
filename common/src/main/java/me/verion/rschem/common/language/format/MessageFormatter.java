package me.verion.rschem.common.language.format;

import lombok.NonNull;
import me.verion.rschem.common.language.Placeholder;
import net.kyori.adventure.text.Component;

/**
 * Transforms a raw message string into a rendered {@link Component}, applying the given {@link Placeholder placeholders}
 * before delivery.
 *
 * @since 2.0
 */
public interface MessageFormatter {

  /**
   * Formats the given raw message string into a {@link Component}, substituting all provided {@link Placeholder}s.
   *
   * @param raw          the raw message string to format.
   * @param placeholders the placeholders to apply to the message before rendering.
   * @return the fully rendered {@link Component}.
   * @throws NullPointerException if the given raw string or placeholders array is null.
   */
  @NonNull
  Component format(@NonNull String raw, @NonNull Placeholder @NonNull ... placeholders);

  /**
   * Clears any internally cached parse results. This method is a no-op by default and only needs to be overridden
   * by implementations that maintain a cache.
   */
  default void clearCache() {
  }
}

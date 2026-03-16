package me.verion.rschem.common.language;

import com.google.common.base.Preconditions;
import lombok.NonNull;

/**
 * Configuration for the {@link I18n} system, defining the default locale and the classpath resource path under which
 * language files are discovered.
 *
 * @param defaultLocale         the locale code used as the fallback when a requested locale is not available.
 * @param languagesResourcePath the classpath-relative directory containing {@code .properties} language files.
 * @since 2.0
 */
public record I18nConfig(@NonNull String defaultLocale, @NonNull String languagesResourcePath) {

  /**
   * Creates a new {@link Builder} with default values pre-applied.
   *
   * @return a new builder instance.
   */
  public static @NonNull Builder builder() {
    return new Builder();
  }

  /**
   * Returns an {@link I18nConfig} instance with all default values applied.
   *
   * @return the default configuration.
   */
  public static @NonNull I18nConfig defaults() {
    return builder().build();
  }

  /**
   * A builder for a {@link I18nConfig}.
   *
   * @since 4.0
   */
  public static final class Builder {

    private String defaultLocale = "en_US";
    private String languagesResourcePath = "languages";

    /**
     * Sets the default locale code to fall back to when a requested locale is not available.
     *
     * @param defaultLocale the default locale code (e.g. {@code "en_US"}).
     * @return the same instance as used to call the method, for chaining.
     * @throws NullPointerException if the given locale is null.
     */
    public @NonNull Builder defaultLocale(@NonNull String defaultLocale) {
      this.defaultLocale = defaultLocale;
      return this;
    }

    /**
     * Sets the classpath-relative directory path under which {@code .properties} language files are discovered.
     *
     * @param languagesResourcePath the resource path (e.g. {@code "languages"}).
     * @return the same instance as used to call the method, for chaining.
     * @throws NullPointerException if the given path is null.
     */
    public @NonNull Builder languagesResourcePath(@NonNull String languagesResourcePath) {
      this.languagesResourcePath = languagesResourcePath;
      return this;
    }

    /**
     * Builds the new config with all previously set options.
     *
     * @return the new config.
     * @throws NullPointerException if the default locale or languages resource path is null.
     */
    public @NonNull I18nConfig build() {
      Preconditions.checkNotNull(this.defaultLocale, "No default locale given");
      Preconditions.checkNotNull(this.languagesResourcePath, "No languages resource path given");

      return new I18nConfig(this.defaultLocale, this.languagesResourcePath);
    }
  }
}

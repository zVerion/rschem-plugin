package me.verion.rschem.internal.schematic;

import lombok.NonNull;
import me.verion.rschem.*;
import me.verion.rschem.exception.SchematicLoadException;
import me.verion.rschem.exception.SchematicWriteException;
import me.verion.rschem.schematic.SchematicService;
import me.verion.rschem.transform.SchematicTransformer;
import me.verion.rschem.validation.ValidationResult;
import me.verion.rschem.validation.type.SchematicValidator;

import java.nio.file.Path;

public final class SchematicServiceImpl implements SchematicService {

  private final SchematicRegistry registry;
  private final SchematicLoader loader;
  private final SchematicWriter writer;
  private final SchematicTransformer transformer;

  /**
   * Constructs a new {@code Rschem} instance, eagerly initializing all schematic subsystems with their respective
   * default configurations.
   *
   * @param pluginMainClass the plugin this facade is bound to, not null
   */
  public SchematicServiceImpl(@NonNull RSchemPlugin pluginMainClass) {
    this.loader = SchematicLoader.create();
    this.writer = SchematicWriter.create(pluginMainClass.configuration().compression());
    this.registry = SchematicRegistry.create(loader);
    this.transformer = SchematicTransformer.create(pluginMainClass);
  }

  /**
   * {@inheritDoc}
   */
  public void save(@NonNull Schematic schematic, @NonNull Path path) throws SchematicWriteException {
    this.writer.write(schematic, path);
  }

  /**
   * {@inheritDoc}
   */
  public @NonNull ValidationResult validate(@NonNull Schematic schematic) {
    return SchematicValidator.validate(schematic);
  }

  /**
   * {@inheritDoc}
   */
  public @NonNull SchematicRegistry registry() {
    return this.registry;
  }

  /**
   * {@inheritDoc}
   */
  public @NonNull SchematicLoader loader() {
    return this.loader;
  }

  /**
   * {@inheritDoc}
   */
  public @NonNull SchematicWriter writer() {
    return this.writer;
  }

  /**
   * {@inheritDoc}
   */
  public @NonNull SchematicTransformer transformer() {
    return this.transformer;
  }

  /**
   * {@inheritDoc}
   */
  public void loadAll(@NonNull Path directory) throws SchematicLoadException {
    this.registry.loadAll(directory);
  }
}

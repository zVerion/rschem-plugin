package me.verion.rschem.schematic;

import lombok.NonNull;
import me.verion.rschem.*;
import me.verion.rschem.exception.SchematicLoadException;
import me.verion.rschem.exception.SchematicWriteException;
import me.verion.rschem.internal.schematic.SchematicServiceImpl;
import me.verion.rschem.transform.SchematicTransformer;
import me.verion.rschem.validation.ValidationResult;
import me.verion.rschem.validation.type.SchematicValidator;
import org.jetbrains.annotations.Contract;

import java.nio.file.Path;

/**
 * Central façade for the rschem API, wiring together the {@link SchematicRegistry}, {@link SchematicLoader},
 * {@link SchematicWriter}, {@link SchematicTransformer} and {@link SchematicValidator} into a single coordinated entry
 * point.
 *
 * <pre>{@code
 * // plugin onEnable
 * SchematicService service = SchematicService.create(this);
 * service.loadAll(dataFolder.toPath().resolve("schematics"));
 * SchematicService.register(service);
 * }</pre>
 * <p>
 * The typical placement workflow composes the individual components directly:
 *
 * <pre>{@code
 * Schematic schematic = service.loader().load(path);
 * Schematic rotated   = service.transformer().rotate(schematic, 90);
 *
 * service.transformer().paste(rotated, world, origin, PasteOptions.builder()
 *   .ignoreAir(true)
 *   .chunkBatchSize(500)
 *   .onComplete(() -> logger.info("done"))
 *   .build());
 * }</pre>
 * <p>
 * The registry and I/O components are thread-safe. Direct world operations — {@link SchematicTransformer#paste} and
 * transformation methods — must be initiated from the server's main thread.
 *
 * @since 2.0
 */
public interface SchematicService {

  /**
   * Creates and returns a new default {@link SchematicService} implementation.
   *
   * @param pluginMainClass the underlying plugin instance for managing schematics
   * @return a new {@link SchematicService} instance
   */
  @Contract("_ -> new")
  @NonNull
  static SchematicService create(@NonNull RSchemPlugin pluginMainClass) {
    return new SchematicServiceImpl(pluginMainClass);
  }

  /**
   * Serializes the given schematic and writes it to the specified file path, using the configured
   * {@link SchematicWriter}.
   *
   * @param schematic the schematic to persist, not null.
   * @param path      the target file path to write the schematic to, not null.
   * @throws NullPointerException    if the given schematic or path is null.
   * @throws SchematicWriteException if the schematic could not be serialized or written.
   */
  void save(@NonNull Schematic schematic, @NonNull Path path) throws SchematicWriteException;

  /**
   * Validates the structural integrity and block data of the given schematic.
   *
   * @param schematic the schematic to validate, not null.
   * @return the {@link ValidationResult} describing the outcome of the validation, never null.
   * @throws NullPointerException if the given schematic is null.
   */
  @Contract(pure = true)
  @NonNull
  ValidationResult validate(@NonNull Schematic schematic);

  /**
   * Returns the {@link SchematicRegistry} that manages all schematics loaded into memory.
   *
   * @return the associated schematic registry, never null.
   */
  @Contract("-> new")
  @NonNull
  SchematicRegistry registry();

  /**
   * Returns the {@link SchematicLoader} responsible for deserializing schematics from persistent storage.
   *
   * @return the associated schematic loader, never null.
   */
  @Contract("-> new")
  @NonNull
  SchematicLoader loader();

  /**
   * Returns the {@link SchematicWriter} responsible for serializing and persisting schematics to disk.
   *
   * @return the associated schematic writer, never null.
   */
  @Contract("-> new")
  @NonNull
  SchematicWriter writer();

  /**
   * Returns the {@link SchematicTransformer} responsible for placing schematics into a Minecraft world.
   *
   * @return the associated schematic transformer, never null.
   */
  @Contract("-> new")
  @NonNull
  SchematicTransformer transformer();

  /**
   * Discovers and loads all schematics present in the given directory into the registry. Files that cannot be parsed
   * are reported via a {@link SchematicLoadException}.
   *
   * @param directory the directory to scan for schematic files, not null.
   * @throws NullPointerException   if the given directory is null.
   * @throws SchematicLoadException if one or more schematic files could not be loaded.
   */
  void loadAll(@NonNull Path directory) throws SchematicLoadException;
}

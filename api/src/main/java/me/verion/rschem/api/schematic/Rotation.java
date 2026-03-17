package me.verion.rschem.api.schematic;

import lombok.NonNull;
import org.jetbrains.annotations.Contract;

/**
 * Defines the clockwise rotation applied to a schematic or structure during placement.
 * <p>
 * All four cardinal rotations are represented as multiples of 90°. Rotations can be combined via {@link #add(Rotation)}
 * and inverted via {@link #inverse()}, making it straightforward to compose or undo transformations without manual
 * degree arithmetic.
 *
 * @see Mirror
 * @since 2.0
 */
public enum Rotation {

  /**
   * No rotation — the schematic is placed exactly as stored.
   */
  NONE(0),

  /**
   * A clockwise rotation of 90°.
   */
  DEGREE_90(90),

  /**
   * A clockwise rotation of 180°.
   */
  DEGREE_180(180),

  /**
   * A clockwise rotation of 270° (equivalent to 90° counter-clockwise).
   */
  DEGREE_270(270);

  private final int degrees;

  Rotation(int degrees) {
    this.degrees = degrees;
  }

  /**
   * Returns the rotation angle in degrees.
   *
   * @return the angle in degrees — one of {@code 0}, {@code 90}, {@code 180}, or {@code 270}
   */
  @Contract(pure = true)
  public int degrees() {
    return this.degrees;
  }

  /**
   * Returns the rotation that results from applying {@code other} after this rotation.
   * <p>
   * The combined angle is calculated as {@code (this.degrees + other.degrees) % 360} and then mapped back to the
   * corresponding constant. The operation is commutative; the order of operands does not affect the result.
   *
   * @param other the rotation to add to this one
   * @return the combined {@link Rotation} constant
   */
  @Contract(pure = true)
  public @NonNull Rotation add(@NonNull Rotation other) {
    return switch ((this.degrees + other.degrees()) % 360) {
      case 0 -> NONE;
      case 90 -> DEGREE_90;
      case 180 -> DEGREE_180;
      case 270 -> DEGREE_270;
      default ->
        throw new IllegalStateException("unexpected combined degrees: " + (this.degrees + other.degrees()) % 360);
    };
  }

  /**
   * Returns the inverse of this rotation such that {@code rotation.add(rotation.inverse()) == NONE}.
   *
   * @return the {@link Rotation} that, when added to this one, yields {@link #NONE}
   */
  @Contract(pure = true)
  public @NonNull Rotation inverse() {
    return switch (this) {
      case NONE -> NONE;
      case DEGREE_90 -> DEGREE_270;
      case DEGREE_180 -> DEGREE_180;
      case DEGREE_270 -> DEGREE_90;
    };
  }
}

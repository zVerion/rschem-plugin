package me.verion.rschem.api.schematic;

/**
 * Defines the axis or axes along which a schematic or structure is mirrored during placement.
 * <p>
 * Mirroring is applied relative to the placement origin before any {@code Rotation} is evaluated. The four constants
 * cover all practically distinct mirror states: no mirroring, a flip along either the X or Z axis, and a simultaneous
 * flip along both axes.
 *
 * @see Rotation
 * @since 2.0
 */
public enum Mirror {

  /**
   * No mirroring — the schematic is placed exactly as stored.
   */
  NONE,

  /**
   * Mirror along the X axis (flips the structure east–west, i.e. negates the X coordinate relative to the placement
   * origin).
   */
  X,

  /**
   * Mirror along the Z axis (flips the structure north–south, i.e. negates the Z coordinate relative to the placement
   * origin).
   */
  Z,

  /**
   * Mirror along both the X and Z axes simultaneously.
   * <p>
   * This is geometrically equivalent to a 180° rotation combined with no mirror, but unlike a pure rotation it
   * preserves the handedness of the structure.
   */
  X_AND_Z
}

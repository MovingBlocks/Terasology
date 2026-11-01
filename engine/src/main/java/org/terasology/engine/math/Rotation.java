// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.math;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import gnu.trove.map.TByteObjectMap;
import gnu.trove.map.hash.TByteObjectHashMap;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;

import java.util.Arrays;
import java.util.List;

/**
 * Rotation provides easy access to 90 degree increments of rotations - intended for block-related rotations.
 * <br><br>
 * Uses the fly weight pattern to cache the 64 combinations of rotation.
 * Note (Marcin Sciesinski): there are actually only 24 possible combinations, the remaining 40 are just duplicates
 *
 */
public final class Rotation {
    private static final Rotation[] ALL_ROTATIONS;
    private static final TByteObjectMap<Rotation> NORMALIZED_ROTATIONS;
    private static final ImmutableList<Rotation> HORIZONTAL_ROTATIONS;
    private static final Rotation[] REVERSE_ROTATIONS;

    private final Yaw yaw;
    private final Pitch pitch;
    private final Roll roll;
    private final Quaternionfc rotation;

    /**
     * The index used by .rotate(), among others. Ranges from 0 to 63 (4^3-1).
     */
    private final int index;

    private Rotation(Yaw yaw, Pitch pitch, Roll roll) {
        this.pitch = pitch;
        this.yaw = yaw;
        this.roll = roll;
        this.index = indexFor(yaw, pitch, roll);
        this.rotation = new Quaternionf().rotationYXZ(yaw.getRadians(), pitch.getRadians(), roll.getRadians());
    }

    public static Rotation none() {
        return rotate(Yaw.NONE, Pitch.NONE, Roll.NONE);
    }

    public static Rotation rotate(Pitch pitch) {
        return rotate(Yaw.NONE, pitch, Roll.NONE);
    }

    public static Rotation rotate(Yaw yaw) {
        return rotate(yaw, Pitch.NONE, Roll.NONE);
    }

    public static Rotation rotate(Roll roll) {
        return rotate(Yaw.NONE, Pitch.NONE, roll);
    }

    public static Rotation rotate(Yaw yaw, Pitch pitch) {
        return rotate(yaw, pitch, Roll.NONE);
    }

    public static Rotation rotate(Pitch pitch, Roll roll) {
        return rotate(Yaw.NONE, pitch, roll);
    }

    public static Rotation rotate(Yaw yaw, Roll roll) {
        return rotate(yaw, Pitch.NONE, roll);
    }

    public static Rotation rotate(Yaw yaw, Pitch pitch, Roll roll) {
        return ALL_ROTATIONS[indexFor(yaw, pitch, roll)];
    }

    /**
     * Finds a reverse rotation to the specified one. Any side transformed by the rotation passed as a parameter, when
     * passed to the returned rotation will return the original Side.
     *
     * @param rotation Rotation to find reverse rotation to.
     *
     * @return Reverse rotation to the specified one.
     */
    public static Rotation findReverse(Rotation rotation) {
        Preconditions.checkNotNull(rotation);
        return REVERSE_ROTATIONS[rotation.index];
    }

    public static List<Rotation> horizontalRotations() {
        return HORIZONTAL_ROTATIONS;
    }

    static {
        ALL_ROTATIONS = new Rotation[4 * 4 * 4];

        NORMALIZED_ROTATIONS = new TByteObjectHashMap<>();
        for (Pitch pitch : Pitch.values()) {
            for (Yaw yaw : Yaw.values()) {
                for (Roll roll : Roll.values()) {
                    Rotation rotation = new Rotation(yaw, pitch, roll);
                    ALL_ROTATIONS[indexFor(yaw, pitch, roll)] = rotation;
                    Byte duplicateIndex = findDuplicateRotation(rotation);
                    if (duplicateIndex == null) {
                        NORMALIZED_ROTATIONS.put(indexFor(yaw, pitch, roll), rotation);
                    }
                }
            }
        }

        HORIZONTAL_ROTATIONS = ImmutableList.of(
                ALL_ROTATIONS[indexFor(Yaw.NONE, Pitch.NONE, Roll.NONE)],
                ALL_ROTATIONS[indexFor(Yaw.CLOCKWISE_90, Pitch.NONE, Roll.NONE)],
                ALL_ROTATIONS[indexFor(Yaw.CLOCKWISE_180, Pitch.NONE, Roll.NONE)],
                ALL_ROTATIONS[indexFor(Yaw.CLOCKWISE_270, Pitch.NONE, Roll.NONE)]);

        REVERSE_ROTATIONS = new Rotation[ALL_ROTATIONS.length];
        for (Rotation rotation : ALL_ROTATIONS) {
            Rotation reverse = findReverseInternal(rotation);
            REVERSE_ROTATIONS[rotation.index] = reverse;
        }
    }

    private static Rotation findReverseInternal(Rotation rotation) {
        Side frontResult = rotation.rotate(Side.FRONT);
        Side topResult = rotation.rotate(Side.TOP);

        for (Rotation possibility : values()) {
            if (possibility.rotate(frontResult) == Side.FRONT
                    && possibility.rotate(topResult) == Side.TOP) {
                return possibility;
            }
        }
        throw new RuntimeException("Unable to find reverse rotation");
    }

    private static Byte findDuplicateRotation(Rotation rotation) {
        Side frontResult = rotation.rotate(Side.FRONT);
        Side topResult = rotation.rotate(Side.TOP);
        byte[] result = new byte[]{127};

        NORMALIZED_ROTATIONS.forEachEntry(
                (a, b) -> {
                    if (b.rotate(Side.FRONT) == frontResult
                            && b.rotate(Side.TOP) == topResult) {
                        result[0] = a;
                        return false;
                    }
                    return true;
                });

        if (result[0] != 127) {
            return result[0];
        } else {
            return null;
        }
    }

    private static byte indexFor(Yaw yaw, Pitch pitch, Roll roll) {
        return (byte) ((yaw.getIndex() << 4) + (pitch.getIndex() << 2) + roll.getIndex());
    }

    public Yaw getYaw() {
        return yaw;
    }

    public Pitch getPitch() {
        return pitch;
    }

    public Roll getRoll() {
        return roll;
    }

    /**
     * The orientation of the current Rotation
     * @return The orientation
     */
    public Quaternionfc orientation() {
        return rotation;
    }

    public Side rotate(Side side) {
        Side result = side;
        result = result.rollClockwise(roll.getIncrements());
        result = result.pitchClockwise(pitch.getIncrements());
        result = result.yawClockwise(yaw.getIncrements());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Rotation) {
            Rotation other = (Rotation) obj;
            return index == other.index;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return index;
    }

    /**
     * Returns all possible rotations for each yaw, pitch &amp; roll combination, including duplicates.
     *
     * @return All possible rotations for each yaw, pitch &amp; roll combination.
     */
    public static Iterable<Rotation> allValues() {
        return Arrays.asList(ALL_ROTATIONS);
    }

    /**
     * Returns only unique rotations, in respect to transformations, rather than in respect to yaw, pitch &amp; roll.
     *
     * @return Unique rotations, in respect to transformations.
     */
    public static Iterable<Rotation> values() {
        return NORMALIZED_ROTATIONS.valueCollection();
    }
}

/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.math;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import gnu.trove.map.TByteObjectMap;
import gnu.trove.map.hash.TByteObjectHashMap;
import org.terasology.math.geom.Quat4f;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Rotation provides easy access to 90 degree increments of rotations - intended for block-related rotations.
 * <br><br>
 * Uses the fly weight pattern to cache the 64 combinations of rotation.
 * Note (Marcin Sciesinski): there are actually only 24 possible combinations, the remaining 40 are just duplicates
 *
 */
public final class Rotation {

    private static final TByteObjectMap<Rotation> ALL_ROTATIONS;
    private static final TByteObjectMap<Rotation> NORMALIZED_ROTATIONS;
    private static final ImmutableList<Rotation> HORIZONTAL_ROTATIONS;
    private static final Map<Rotation, Rotation> REVERSE_ROTATIONS_MAP;

    private Yaw yaw;
    private Pitch pitch;
    private Roll roll;

    private Rotation(Yaw yaw, Pitch pitch, Roll roll) {
        this.pitch = pitch;
        this.yaw = yaw;
        this.roll = roll;
    }

    public static Rotation none() {
        return ALL_ROTATIONS.get(indexFor(Yaw.NONE, Pitch.NONE, Roll.NONE));
    }

    public static Rotation rotate(Pitch pitch) {
        return ALL_ROTATIONS.get(indexFor(Yaw.NONE, pitch, Roll.NONE));
    }

    public static Rotation rotate(Yaw yaw) {
        return ALL_ROTATIONS.get(indexFor(yaw, Pitch.NONE, Roll.NONE));
    }

    public static Rotation rotate(Roll roll) {
        return ALL_ROTATIONS.get(indexFor(Yaw.NONE, Pitch.NONE, roll));
    }

    public static Rotation rotate(Yaw yaw, Pitch pitch) {
        return ALL_ROTATIONS.get(indexFor(yaw, pitch, Roll.NONE));
    }

    public static Rotation rotate(Pitch pitch, Roll roll) {
        return ALL_ROTATIONS.get(indexFor(Yaw.NONE, pitch, roll));
    }

    public static Rotation rotate(Yaw yaw, Roll roll) {
        return ALL_ROTATIONS.get(indexFor(yaw, Pitch.NONE, roll));
    }

    public static Rotation rotate(Yaw yaw, Pitch pitch, Roll roll) {
        return ALL_ROTATIONS.get(indexFor(yaw, pitch, roll));
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
        return REVERSE_ROTATIONS_MAP.get(rotation);
    }

    public static List<Rotation> horizontalRotations() {
        return HORIZONTAL_ROTATIONS;
    }

    static {
        ALL_ROTATIONS = new TByteObjectHashMap<>();
        NORMALIZED_ROTATIONS = new TByteObjectHashMap<>();
        for (Pitch pitch : Pitch.values()) {
            for (Yaw yaw : Yaw.values()) {
                for (Roll roll : Roll.values()) {
                    Rotation rotation = new Rotation(yaw, pitch, roll);
                    ALL_ROTATIONS.put(indexFor(yaw, pitch, roll), new Rotation(yaw, pitch, roll));
                    Byte duplicateIndex = findDuplicateRotation(rotation);
                    if (duplicateIndex == null) {
                        NORMALIZED_ROTATIONS.put(indexFor(yaw, pitch, roll), rotation);
                    }
                }
            }
        }

        HORIZONTAL_ROTATIONS = ImmutableList.of(
                ALL_ROTATIONS.get(indexFor(Yaw.NONE, Pitch.NONE, Roll.NONE)),
                ALL_ROTATIONS.get(indexFor(Yaw.CLOCKWISE_90, Pitch.NONE, Roll.NONE)),
                ALL_ROTATIONS.get(indexFor(Yaw.CLOCKWISE_180, Pitch.NONE, Roll.NONE)),
                ALL_ROTATIONS.get(indexFor(Yaw.CLOCKWISE_270, Pitch.NONE, Roll.NONE)));

        REVERSE_ROTATIONS_MAP = new HashMap<>();
        for (Rotation rotation : ALL_ROTATIONS.valueCollection()) {
            Rotation reverse = findReverseInternal(rotation);
            REVERSE_ROTATIONS_MAP.put(rotation, reverse);
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

    public Quat4f getQuat4f() {
        Quat4f rotation = new Quat4f(yaw.getRadians(), pitch.getRadians(), roll.getRadians());
        rotation.normalize();
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
            return yaw == other.yaw && pitch == other.pitch && roll == other.roll;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(yaw, pitch, roll);
    }

    /**
     * Returns all possible rotations for each yaw, pitch & roll combination, including duplicates.
     *
     * @return All possible rotations for each yaw, pitch & roll combination.
     */
    public static Iterable<Rotation> allValues() {
        return ALL_ROTATIONS.valueCollection();
    }

    /**
     * Returns only unique rotations, in respect to transformations, rather than in respect to yaw, pitch & roll.
     *
     * @return Unique rotations, in respect to transformations.
     */
    public static Iterable<Rotation> values() {
        return NORMALIZED_ROTATIONS.valueCollection();
    }
}

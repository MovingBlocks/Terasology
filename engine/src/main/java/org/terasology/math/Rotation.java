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

import com.bulletphysics.linearmath.QuaternionUtil;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import gnu.trove.map.TByteObjectMap;
import gnu.trove.map.hash.TByteObjectHashMap;

import javax.vecmath.Quat4f;
import java.util.List;

/**
 * Rotation provides easy access to 90 degree increments of rotations - intended for block-related rotations.
 * <p/>
 * Uses the fly weight pattern to cache the 64 combinations of rotation.
 *
 * @author Immortius <immortius@gmail.com>
 */
public final class Rotation {

    private static final TByteObjectMap<Rotation> ROTATIONS;
    private static final ImmutableList<Rotation> HORIZONTAL_ROTATIONS;

    private Yaw yaw;
    private Pitch pitch;
    private Roll roll;

    private Rotation(Yaw yaw, Pitch pitch, Roll roll) {
        this.pitch = pitch;
        this.yaw = yaw;
        this.roll = roll;
    }

    public static Rotation none() {
        return ROTATIONS.get(indexFor(Yaw.NONE, Pitch.NONE, Roll.NONE));
    }

    public static Rotation rotate(Pitch pitch) {
        return ROTATIONS.get(indexFor(Yaw.NONE, pitch, Roll.NONE));
    }

    public static Rotation rotate(Yaw yaw) {
        return ROTATIONS.get(indexFor(yaw, Pitch.NONE, Roll.NONE));
    }

    public static Rotation rotate(Roll roll) {
        return ROTATIONS.get(indexFor(Yaw.NONE, Pitch.NONE, roll));
    }

    public static Rotation rotate(Yaw yaw, Pitch pitch) {
        return ROTATIONS.get(indexFor(yaw, pitch, Roll.NONE));
    }

    public static Rotation rotate(Pitch pitch, Roll roll) {
        return ROTATIONS.get(indexFor(Yaw.NONE, pitch, roll));
    }

    public static Rotation rotate(Yaw yaw, Roll roll) {
        return ROTATIONS.get(indexFor(yaw, Pitch.NONE, roll));
    }

    public static Rotation rotate(Yaw yaw, Pitch pitch, Roll roll) {
        return ROTATIONS.get(indexFor(yaw, pitch, roll));
    }

    public static List<Rotation> horizontalRotations() {
        return HORIZONTAL_ROTATIONS;
    }

    static {
        ROTATIONS = new TByteObjectHashMap<>();
        for (Pitch pitch : Pitch.values()) {
            for (Yaw yaw : Yaw.values()) {
                for (Roll roll : Roll.values()) {
                    ROTATIONS.put(indexFor(yaw, pitch, roll), new Rotation(yaw, pitch, roll));
                }
            }
        }
        HORIZONTAL_ROTATIONS = ImmutableList.of(
                ROTATIONS.get(indexFor(Yaw.NONE, Pitch.NONE, Roll.NONE)),
                ROTATIONS.get(indexFor(Yaw.CLOCKWISE_90, Pitch.NONE, Roll.NONE)),
                ROTATIONS.get(indexFor(Yaw.CLOCKWISE_180, Pitch.NONE, Roll.NONE)),
                ROTATIONS.get(indexFor(Yaw.CLOCKWISE_270, Pitch.NONE, Roll.NONE)));
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
        Quat4f rotation = new Quat4f();
        QuaternionUtil.setEuler(rotation, yaw.getRadians(), pitch.getRadians(), roll.getRadians());
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

    public static Iterable<Rotation> values() {
        return ROTATIONS.valueCollection();
    }
}

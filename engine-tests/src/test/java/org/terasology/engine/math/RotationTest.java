// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.math;

import com.google.common.collect.Iterables;
import org.junit.jupiter.api.Test;
import org.terasology.engine.math.Direction;
import org.terasology.engine.math.Pitch;
import org.terasology.engine.math.Roll;
import org.terasology.engine.math.Rotation;
import org.terasology.engine.math.Side;
import org.terasology.engine.math.Yaw;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector3f;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 */
public class RotationTest {

    @Test
    public void testRotateSideNone() {
        assertEquals(Side.LEFT, Rotation.none().rotate(Side.LEFT));
    }

    @Test
    public void testRotateSideYaw() {
        Rotation rotation = Rotation.rotate(Yaw.CLOCKWISE_90);
        Quat4f rot = rotation.getQuat4f();
        Vector3f dir = rot.rotate(Side.FRONT.toDirection().getVector3f(), new Vector3f());
        assertEquals(Direction.inDirection(dir).toSide(), rotation.rotate(Side.FRONT));

        assertEquals(Side.LEFT, Rotation.rotate(Yaw.CLOCKWISE_90).rotate(Side.FRONT));
        assertEquals(Side.TOP, Rotation.rotate(Yaw.CLOCKWISE_90).rotate(Side.TOP));
    }

    @Test
    public void testRotateSidePitch() {
        Rotation rotation = Rotation.rotate(Pitch.CLOCKWISE_90);
        Quat4f rot = rotation.getQuat4f();
        Vector3f dir = rot.rotate(Side.FRONT.toDirection().getVector3f(), new Vector3f());
        assertEquals(Direction.inDirection(dir).toSide(), rotation.rotate(Side.FRONT));

        assertEquals(Side.TOP, Rotation.rotate(Pitch.CLOCKWISE_90).rotate(Side.FRONT));
        assertEquals(Side.RIGHT, Rotation.rotate(Pitch.CLOCKWISE_90).rotate(Side.RIGHT));
    }

    @Test
    public void testRotateSideRoll() {
        Rotation rotation = Rotation.rotate(Roll.CLOCKWISE_90);
        Quat4f rot = rotation.getQuat4f();
        Vector3f dir = rot.rotate(Side.TOP.toDirection().getVector3f(), new Vector3f());
        assertEquals(Direction.inDirection(dir).toSide(), rotation.rotate(Side.TOP));

        assertEquals(Side.LEFT, Rotation.rotate(Roll.CLOCKWISE_90).rotate(Side.TOP));
        assertEquals(Side.FRONT, Rotation.rotate(Roll.CLOCKWISE_90).rotate(Side.FRONT));
    }

    @Test
    public void testRotateMixed() {
        Rotation rotation = Rotation.rotate(Yaw.CLOCKWISE_180, Pitch.CLOCKWISE_90, Roll.CLOCKWISE_90);
        Quat4f rot = rotation.getQuat4f();
        Vector3f dir = rot.rotate(Side.FRONT.toDirection().getVector3f(), new Vector3f());
        assertEquals(Direction.inDirection(dir).toSide(), rotation.rotate(Side.FRONT));
    }

    @Test
    public void testAllRotations() {
        assertEquals(24, Iterables.size(Rotation.values()));
        assertEquals(64, Iterables.size(Rotation.allValues()));
    }

    @Test
    public void testReverseRotation() {
        for (Rotation rotation : Rotation.allValues()) {
            Rotation reverseRotation = Rotation.findReverse(rotation);
            for (Side side : Side.getAllSides()) {
                assertEquals(side, reverseRotation.rotate(rotation.rotate(side)));
            }
        }
    }
}

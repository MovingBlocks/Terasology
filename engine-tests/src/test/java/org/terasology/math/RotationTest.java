// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.math;

import com.google.common.collect.Iterables;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;
import org.terasology.engine.math.Direction;
import org.terasology.engine.math.Pitch;
import org.terasology.engine.math.Roll;
import org.terasology.engine.math.Rotation;
import org.terasology.engine.math.Side;
import org.terasology.engine.math.Yaw;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.terasology.joml.test.QuaternionAssert.assertEquals;

public class RotationTest {

    @Test
    public void testRotateSideNone() {
        assertEquals(Side.LEFT, Rotation.none().rotate(Side.LEFT));
    }

    @Test
    public void testRotateSideYaw() {
        Rotation rotation = Rotation.rotate(Yaw.CLOCKWISE_90);
        Quaternionfc rot = rotation.orientation();
        Vector3f dir = rot.transform(Side.FRONT.toDirection().asVector3f(), new Vector3f());
        assertEquals(Direction.inDirection(dir).toSide(), rotation.rotate(Side.FRONT));

        assertEquals(Side.LEFT, Rotation.rotate(Yaw.CLOCKWISE_90).rotate(Side.FRONT));
        assertEquals(Side.TOP, Rotation.rotate(Yaw.CLOCKWISE_90).rotate(Side.TOP));
    }

    @Test
    public void testOrientation() {
        assertEquals(new Quaternionf().rotationYXZ(90.0f * TeraMath.DEG_TO_RAD, 0, 0), Rotation.rotate(Yaw.CLOCKWISE_90).orientation(), 0.001f);
        assertEquals(new Quaternionf().rotationYXZ(180.0f * TeraMath.DEG_TO_RAD, 0, 0), Rotation.rotate(Yaw.CLOCKWISE_180).orientation(), 0.001f);

        assertEquals(new Quaternionf().rotationYXZ(0, 90.0f * TeraMath.DEG_TO_RAD, 0), Rotation.rotate(Pitch.CLOCKWISE_90).orientation(), 0.001f);
        assertEquals(new Quaternionf().rotationYXZ(0, 180.0f * TeraMath.DEG_TO_RAD, 0), Rotation.rotate(Pitch.CLOCKWISE_180).orientation(), 0.001f);

        assertEquals(new Quaternionf().rotationYXZ(0, 0, 90.0f * TeraMath.DEG_TO_RAD), Rotation.rotate(Roll.CLOCKWISE_90).orientation(), 0.001f);
        assertEquals(new Quaternionf().rotationYXZ(0, 0, 180.0f * TeraMath.DEG_TO_RAD), Rotation.rotate(Roll.CLOCKWISE_180).orientation(), 0.001f);
    }

    @Test
    public void testRotateSidePitch() {
        Rotation rotation = Rotation.rotate(Pitch.CLOCKWISE_90);
        Quaternionfc rot = rotation.orientation();
        Vector3f dir = rot.transform(Side.FRONT.toDirection().asVector3f(), new Vector3f());
        assertEquals(Direction.inDirection(dir).toSide(), rotation.rotate(Side.FRONT));

        assertEquals(Side.TOP, Rotation.rotate(Pitch.CLOCKWISE_90).rotate(Side.FRONT));
        assertEquals(Side.RIGHT, Rotation.rotate(Pitch.CLOCKWISE_90).rotate(Side.RIGHT));
    }

    @Test
    public void testRotateSideRoll() {
        Rotation rotation = Rotation.rotate(Roll.CLOCKWISE_90);
        Quaternionfc rot = rotation.orientation();
        Vector3f dir = rot.transform(Side.TOP.toDirection().asVector3f(), new Vector3f());
        assertEquals(Direction.inDirection(dir).toSide(), rotation.rotate(Side.TOP));

        assertEquals(Side.LEFT, Rotation.rotate(Roll.CLOCKWISE_90).rotate(Side.TOP));
        assertEquals(Side.FRONT, Rotation.rotate(Roll.CLOCKWISE_90).rotate(Side.FRONT));
    }

    @Test
    public void testRotateMixed() {
        Rotation rotation = Rotation.rotate(Yaw.CLOCKWISE_180, Pitch.CLOCKWISE_90, Roll.CLOCKWISE_90);
        Quaternionfc rot = rotation.orientation();
        Vector3f dir = rot.transform(Side.FRONT.toDirection().asVector3f(), new Vector3f());
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

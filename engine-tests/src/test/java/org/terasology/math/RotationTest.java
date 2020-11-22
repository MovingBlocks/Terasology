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

import com.google.common.collect.Iterables;
import org.joml.Quaternionf;
import org.junit.jupiter.api.Test;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector3f;
import org.terasology.testUtil.TeraAssert;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
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
    public void testOrientation() {
        TeraAssert.assertEquals(new Quaternionf().rotationYXZ(90.0f * TeraMath.DEG_TO_RAD,0,0), Rotation.rotate(Yaw.CLOCKWISE_90).orientation(),0.001f);
        TeraAssert.assertEquals(new Quaternionf().rotationYXZ(180.0f * TeraMath.DEG_TO_RAD,0,0), Rotation.rotate(Yaw.CLOCKWISE_180).orientation(),0.001f);

        TeraAssert.assertEquals(new Quaternionf().rotationYXZ(0,90.0f * TeraMath.DEG_TO_RAD,0), Rotation.rotate(Pitch.CLOCKWISE_90).orientation(),0.001f);
        TeraAssert.assertEquals(new Quaternionf().rotationYXZ(0,180.0f * TeraMath.DEG_TO_RAD,0), Rotation.rotate(Pitch.CLOCKWISE_180).orientation(),0.001f);

        TeraAssert.assertEquals(new Quaternionf().rotationYXZ(0,0,90.0f * TeraMath.DEG_TO_RAD), Rotation.rotate(Roll.CLOCKWISE_90).orientation(),0.001f);
        TeraAssert.assertEquals(new Quaternionf().rotationYXZ(0,0,180.0f * TeraMath.DEG_TO_RAD), Rotation.rotate(Roll.CLOCKWISE_180).orientation(),0.001f);
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

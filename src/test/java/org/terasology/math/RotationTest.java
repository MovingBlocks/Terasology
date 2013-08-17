/*
 * Copyright 2013 Moving Blocks
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
import org.junit.Test;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import static org.junit.Assert.assertEquals;

/**
 * @author Immortius
 */
public class RotationTest {

    @Test
    public void rotateSideNone() {
        assertEquals(Side.LEFT, Rotation.none().rotate(Side.LEFT));
    }

    @Test
    public void rotateSideYaw() {
        Rotation rotation = Rotation.rotate(Yaw.CLOCKWISE_90);
        Quat4f rot = rotation.getQuat4f();
        Vector3f dir = QuaternionUtil.quatRotate(rot, Side.FRONT.toDirection().getVector3f(), new Vector3f());
        assertEquals(Direction.inDirection(dir).toSide(), rotation.rotate(Side.FRONT));

        assertEquals(Side.LEFT, Rotation.rotate(Yaw.CLOCKWISE_90).rotate(Side.FRONT));
        assertEquals(Side.TOP, Rotation.rotate(Yaw.CLOCKWISE_90).rotate(Side.TOP));
    }

    @Test
    public void rotateSidePitch() {
        Rotation rotation = Rotation.rotate(Pitch.CLOCKWISE_90);
        Quat4f rot = rotation.getQuat4f();
        Vector3f dir = QuaternionUtil.quatRotate(rot, Side.FRONT.toDirection().getVector3f(), new Vector3f());
        assertEquals(Direction.inDirection(dir).toSide(), rotation.rotate(Side.FRONT));

        assertEquals(Side.TOP, Rotation.rotate(Pitch.CLOCKWISE_90).rotate(Side.FRONT));
        assertEquals(Side.RIGHT, Rotation.rotate(Pitch.CLOCKWISE_90).rotate(Side.RIGHT));
    }

    @Test
    public void rotateSideRoll() {
        Rotation rotation = Rotation.rotate(Roll.CLOCKWISE_90);
        Quat4f rot = rotation.getQuat4f();
        Vector3f dir = QuaternionUtil.quatRotate(rot, Side.TOP.toDirection().getVector3f(), new Vector3f());
        assertEquals(Direction.inDirection(dir).toSide(), rotation.rotate(Side.TOP));

        assertEquals(Side.LEFT, Rotation.rotate(Roll.CLOCKWISE_90).rotate(Side.TOP));
        assertEquals(Side.FRONT, Rotation.rotate(Roll.CLOCKWISE_90).rotate(Side.FRONT));
    }

    @Test
    public void rotateMixed() {
        Rotation rotation = Rotation.rotate(Yaw.CLOCKWISE_180, Pitch.CLOCKWISE_90, Roll.CLOCKWISE_90);
        Quat4f rot = rotation.getQuat4f();
        Vector3f dir = QuaternionUtil.quatRotate(rot, Side.FRONT.toDirection().getVector3f(), new Vector3f());
        assertEquals(Direction.inDirection(dir).toSide(), rotation.rotate(Side.FRONT));
    }
}

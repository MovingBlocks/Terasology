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

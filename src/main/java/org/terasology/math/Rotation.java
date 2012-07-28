package org.terasology.math;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

/**
 * @author Immortius <immortius@gmail.com>
 */
public enum Rotation {
    NONE(getQuaternionForHorizRot(0)) {
        @Override
        public Side rotate(Side side) {
            return side;
        }

        @Override
        public AABB rotate(AABB aabb) {
            return aabb;
        }
    },
    HORIZONTAL_CLOCKWISE(getQuaternionForHorizRot(1)) {
        @Override
        public Side rotate(Side side) {
            return side.rotateClockwise(1);
        }

        @Override
        public AABB rotate(AABB aabb) {
            return rotateHorizontalAABB(aabb, 1);
        }
    },
    HORIZONTAL_180(getQuaternionForHorizRot(2)) {
        @Override
        public Side rotate(Side side) {
            return side.rotateClockwise(2);
        }

        @Override
        public AABB rotate(AABB aabb) {
            return rotateHorizontalAABB(aabb, 2);
        }
    },
    HORIZONTAL_ANTI_CLOCKWISE(getQuaternionForHorizRot(3)) {
        @Override
        public Side rotate(Side side) {
            return side.rotateClockwise(3);
        }

        @Override
        public AABB rotate(AABB aabb) {
            return rotateHorizontalAABB(aabb, 3);
        }
    };

    private static Rotation[] horizontalRotations = new Rotation[]{NONE, HORIZONTAL_CLOCKWISE, HORIZONTAL_180, HORIZONTAL_ANTI_CLOCKWISE};

    public static Rotation[] horizontalRotations() {
        return horizontalRotations;
    }

    private Quat4f quat4f;

    public abstract Side rotate(Side side);

    public abstract AABB rotate(AABB aabb);

    private Rotation(Quat4f quat4f) {
        this.quat4f = quat4f;
    }

    public Quat4f getQuat4f() {
        return quat4f;
    }

    private static Quat4f getQuaternionForHorizRot(int steps) {
        Quat4f rotation = new Quat4f();
        rotation.set(new AxisAngle4f(new Vector3f(0, -1, 0), (float) (0.5f * Math.PI * steps)));
        return rotation;
    }

    private static AABB rotateHorizontalAABB(AABB collider, int clockwiseSteps) {
        if (clockwiseSteps < 0) {
            clockwiseSteps = -clockwiseSteps + 2;
        }
        clockwiseSteps = clockwiseSteps % 4;
        Vector3f center = collider.getCenter();
        Vector3f extents = collider.getExtents();
        switch (clockwiseSteps) {
            case 1:
                return AABB.createCenterExtent(new Vector3f(-center.z, center.y, center.x), new Vector3f(extents.z, extents.y, extents.x));
            case 2:
                return AABB.createCenterExtent(new Vector3f(-center.x, center.y, -center.z), extents);
            case 3:
                return AABB.createCenterExtent(new Vector3f(center.z, center.y, -center.x), new Vector3f(extents.z, extents.y, extents.x));
            default:
                return collider;
        }
    }
}

package org.terasology.math;

import org.terasology.model.structures.AABB;

import javax.vecmath.*;

/**
 * @author Immortius <immortius@gmail.com>
 */
public enum Rotation {
    None (getQuaternionForHorizRot(0)){
        @Override
        public Side rotate(Side side) {
            return side;
        }

        @Override
        public AABB rotate(AABB aabb) {
            return aabb;
        }
    },
    HorizontalClockwise (getQuaternionForHorizRot(1)) {
        @Override
        public Side rotate(Side side) {
            return side.rotateClockwise(1);
        }

        @Override
        public AABB rotate(AABB aabb) {
            return rotateHorizontalAABB(aabb, 1);
        }
    },
    Horizontal180 (getQuaternionForHorizRot(2)) {
        @Override
        public Side rotate(Side side) {
            return side.rotateClockwise(2);
        }

        @Override
        public AABB rotate(AABB aabb) {
            return rotateHorizontalAABB(aabb, 2);
        }
    },
    HorizontalAntiClockwise (getQuaternionForHorizRot(3)) {
        @Override
        public Side rotate(Side side) {
            return side.rotateClockwise(3);
        }

        @Override
        public AABB rotate(AABB aabb) {
            return rotateHorizontalAABB(aabb, 3);
        }
    };

    private static Rotation[] horizontalRotations = new Rotation[] {None, HorizontalClockwise, Horizontal180, HorizontalAntiClockwise};

    public static Rotation[] horizontalRotations() { return horizontalRotations; }
    
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
        switch (clockwiseSteps) {
            case 1:
                return new AABB(new Vector3d(-collider.getPosition().z, collider.getPosition().y, collider.getPosition().x), new Vector3d(collider.getDimensions().z, collider.getDimensions().y, collider.getDimensions().x));
            case 2:
                return new AABB(new Vector3d(-collider.getPosition().x, collider.getPosition().y, -collider.getPosition().z), collider.getDimensions());
            case 3:
                return new AABB(new Vector3d(collider.getPosition().z, collider.getPosition().y, -collider.getPosition().x), new Vector3d(collider.getDimensions().z, collider.getDimensions().y, collider.getDimensions().x));
            default:
                return collider;
        }
    }
}

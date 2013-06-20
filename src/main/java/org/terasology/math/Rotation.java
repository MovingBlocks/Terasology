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

import org.terasology.world.block.BlockPart;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Matrix3f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

/**
 * @author Immortius <immortius@gmail.com>
 */
public abstract class Rotation {
    public static final Rotation NONE = new Rotation(getQuaternionForYAxisRot(0)) {
        @Override
        public Side rotate(Side side) {
            return side;
        }

        @Override
        public AABB rotate(AABB aabb) {
            return aabb;
        }
    };
    public static final Rotation HORIZONTAL_CLOCKWISE = new Rotation(getQuaternionForYAxisRot(1)) {
        @Override
        public Side rotate(Side side) {
            return side.rotateClockwise(1);
        }

        @Override
        public AABB rotate(AABB aabb) {
            return rotateYAxisAABB(aabb, 1);
        }
    };
    public static final Rotation HORIZONTAL_180 = new Rotation(getQuaternionForYAxisRot(2)) {
        @Override
        public Side rotate(Side side) {
            return side.rotateClockwise(2);
        }

        @Override
        public AABB rotate(AABB aabb) {
            return rotateYAxisAABB(aabb, 2);
        }
    };
    public static final Rotation HORIZONTAL_ANTI_CLOCKWISE = new Rotation(getQuaternionForYAxisRot(3)) {
        @Override
        public Side rotate(Side side) {
            return side.rotateClockwise(3);
        }

        @Override
        public AABB rotate(AABB aabb) {
            return rotateYAxisAABB(aabb, 3);
        }
    };

    public static Rotation constructTempRotation(final Matrix3f transformation) {
        Quat4f quaternion = new Quat4f();
        quaternion.set(transformation);
        return new Rotation(quaternion) {
            @Override
            public Side rotate(Side side) {
                Vector3f directionVector = side.getVector3i().toVector3f();
                transformation.transform(directionVector);
                return Side.inDirection(directionVector);
            }

            @Override
            public AABB rotate(AABB aabb) {
                Vector3f transformedCenter = new Vector3f();
                transformation.transform(aabb.getCenter(), transformedCenter);
                Vector3f transformedExtent = new Vector3f();
                transformation.transform(aabb.getExtents(), transformedExtent);
                return AABB.createCenterExtent(transformedCenter,
                        new Vector3f(TeraMath.fastAbs(transformedExtent.x), TeraMath.fastAbs(transformedExtent.y), TeraMath.fastAbs(transformedExtent.z)));
            }
        };
    }

    private static Rotation[] horizontalRotations = new Rotation[]{NONE, HORIZONTAL_CLOCKWISE, HORIZONTAL_180, HORIZONTAL_ANTI_CLOCKWISE};

    public static Rotation[] horizontalRotations() {
        return horizontalRotations;
    }

    private Quat4f quat4f;

    public abstract Side rotate(Side side);

    public BlockPart rotate(BlockPart part) {
        if (part.isSide()) {
            return BlockPart.fromSide(rotate(part.getSide()));
        }
        return part;
    }

    public abstract AABB rotate(AABB aabb);

    private Rotation(Quat4f quat4f) {
        this.quat4f = quat4f;
    }

    public Quat4f getQuat4f() {
        return quat4f;
    }

    private static Quat4f getQuaternionForYAxisRot(int steps) {
        Quat4f rotation = new Quat4f();
        rotation.set(new AxisAngle4f(new Vector3f(0, -1, 0), (float) (0.5f * Math.PI * steps)));
        return rotation;
    }

    private static AABB rotateYAxisAABB(AABB collider, int clockwiseSteps) {
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

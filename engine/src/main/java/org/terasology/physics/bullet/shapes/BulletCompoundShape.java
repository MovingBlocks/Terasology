/*
 * Copyright 2017 MovingBlocks
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
package org.terasology.physics.bullet.shapes;


import com.bulletphysics.collision.shapes.CompoundShape;
import com.bulletphysics.collision.shapes.CompoundShapeChild;
import com.bulletphysics.linearmath.Transform;
import org.terasology.math.Rotation;
import org.terasology.math.VecMath;
import org.terasology.math.geom.Quat4f;
import org.terasology.physics.shapes.CollisionShape;

import java.util.ArrayList;
import java.util.List;

public class BulletCompoundShape extends BulletCollisionShape implements org.terasology.physics.shapes.CompoundShape {
    private final CompoundShape compoundShape;
    private List<BulletCompoundShapeChild> childList;

    public BulletCompoundShape() {
        this(new CompoundShape());
    }

    private BulletCompoundShape(CompoundShape compoundShape) {
        this.compoundShape = compoundShape;
        underlyingShape = compoundShape;

        childList = new ArrayList<>();
    }

    @Override
    public void addChildShape(org.terasology.math.Transform transform, CollisionShape collisionShape) {
        BulletCollisionShape bulletCollisionShape = (BulletCollisionShape) collisionShape;
        Transform bulletTransform = toBulletTransform(transform);
        compoundShape.addChildShape(bulletTransform, bulletCollisionShape.underlyingShape);

        childList.add(new BulletCompoundShapeChild(bulletTransform, bulletCollisionShape,
                compoundShape.getChildList().get(compoundShape.getNumChildShapes() - 1)));
    }

    // TODO: Add removeChildShape if needed

    @Override
    public CollisionShape rotate(Quat4f rot) {
        CompoundShape newShape = new CompoundShape();
        for (BulletCompoundShapeChild child : childList) {
            CollisionShape rotatedChild = child.childShape.rotate(rot);
            javax.vecmath.Vector3f offset = com.bulletphysics.linearmath.QuaternionUtil.quatRotate(VecMath.to(rot), child.transform.origin, new javax.vecmath.Vector3f());
            newShape.addChildShape(new Transform(new javax.vecmath.Matrix4f(VecMath.to(Rotation.none().getQuat4f()), offset, 1.0f)), ((BulletCollisionShape) rotatedChild).underlyingShape);
        }
        return new BulletCompoundShape(newShape);
    }

    private static class BulletCompoundShapeChild {
        public Transform transform;
        public BulletCollisionShape childShape;

        public CompoundShapeChild compoundShapeChild;

        private BulletCompoundShapeChild(Transform transform, BulletCollisionShape childShape, CompoundShapeChild compoundShapeChild) {
            this.transform = transform;
            this.childShape = childShape;
            this.compoundShapeChild = compoundShapeChild;
        }
    }
}

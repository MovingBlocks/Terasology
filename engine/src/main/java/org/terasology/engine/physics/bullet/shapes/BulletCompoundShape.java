// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.physics.bullet.shapes;


import com.bulletphysics.collision.shapes.CompoundShape;
import com.bulletphysics.collision.shapes.CompoundShapeChild;
import com.bulletphysics.linearmath.Transform;
import org.terasology.engine.math.Rotation;
import org.terasology.engine.math.VecMath;
import org.terasology.math.geom.Quat4f;
import org.terasology.engine.physics.shapes.CollisionShape;

import java.util.ArrayList;
import java.util.List;

public class BulletCompoundShape extends BulletCollisionShape implements org.terasology.engine.physics.shapes.CompoundShape {
    private final CompoundShape compoundShape;
    private final List<BulletCompoundShapeChild> childList;

    public BulletCompoundShape() {
        this(new CompoundShape());
    }

    private BulletCompoundShape(CompoundShape compoundShape) {
        this.compoundShape = compoundShape;
        underlyingShape = compoundShape;

        childList = new ArrayList<>();
    }

    @Override
    public void addChildShape(org.terasology.engine.math.Transform transform, CollisionShape collisionShape) {
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

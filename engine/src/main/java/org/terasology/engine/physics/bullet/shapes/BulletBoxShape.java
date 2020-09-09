// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.physics.bullet.shapes;

import com.bulletphysics.collision.shapes.BoxShape;
import org.terasology.engine.math.VecMath;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector3f;
import org.terasology.engine.physics.shapes.CollisionShape;

public class BulletBoxShape extends BulletCollisionShape implements org.terasology.engine.physics.shapes.BoxShape {
    private final BoxShape boxShape;

    public BulletBoxShape(Vector3f halfExtents) {
        this(VecMath.to(halfExtents));
    }

    private BulletBoxShape(javax.vecmath.Vector3f halfExtents) {
        boxShape = new BoxShape(halfExtents);
        underlyingShape = boxShape;
    }

    @Override
    public CollisionShape rotate(Quat4f rot) {
        javax.vecmath.Vector3f halfExtentsWithMargin =
                boxShape.getHalfExtentsWithMargin(new javax.vecmath.Vector3f());
        com.bulletphysics.linearmath.QuaternionUtil.quatRotate(VecMath.to(rot), halfExtentsWithMargin, halfExtentsWithMargin);
        halfExtentsWithMargin.absolute();
        return new BulletBoxShape(halfExtentsWithMargin);
    }

    @Override
    public Vector3f getExtents() {
        javax.vecmath.Vector3f out = new javax.vecmath.Vector3f();
        return VecMath.from(boxShape.getHalfExtentsWithoutMargin(out)).scale(2);
    }
}

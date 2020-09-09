// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.physics.bullet.shapes;


import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.linearmath.Transform;
import org.terasology.engine.math.AABB;
import org.terasology.engine.math.VecMath;

public abstract class BulletCollisionShape implements org.terasology.engine.physics.shapes.CollisionShape {
    public CollisionShape underlyingShape;

    @Override
    public AABB getAABB(org.terasology.engine.math.Transform transform) {
        Transform t = toBulletTransform(transform);

        javax.vecmath.Vector3f min = new javax.vecmath.Vector3f();
        javax.vecmath.Vector3f max = new javax.vecmath.Vector3f();
        underlyingShape.getAabb(t, min, max);

        return AABB.createMinMax(VecMath.from(min), VecMath.from(max));
    }

    protected static Transform toBulletTransform(org.terasology.engine.math.Transform transform) {
        return new Transform(
                new javax.vecmath.Matrix4f(VecMath.to(transform.rotation),
                        VecMath.to(transform.origin), transform.scale)
        );
    }
}

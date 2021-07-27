// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.physics.bullet.shapes;


import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import org.terasology.joml.geom.AABBf;
import org.joml.Matrix4f;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.terasology.engine.physics.shapes.CollisionShape;

public abstract class BulletCollisionShape implements CollisionShape {
    public btCollisionShape underlyingShape;

    @Override
    public AABBf getAABB(Vector3fc origin, Quaternionfc rotation, float scale) {

        Vector3f min = new Vector3f();
        Vector3f max = new Vector3f();
        Matrix4f m = new Matrix4f();
        underlyingShape.getAabb(m, min, max);

        return new AABBf(min, max).translate(origin);
    }

}

// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.physics.bullet.shapes;

import com.bulletphysics.collision.shapes.SphereShape;
import org.terasology.math.geom.Quat4f;
import org.terasology.engine.physics.shapes.CollisionShape;

public class BulletSphereShape extends BulletCollisionShape implements org.terasology.engine.physics.shapes.SphereShape {
    private final SphereShape sphereShape;

    public BulletSphereShape(float radius) {
        sphereShape = new SphereShape(radius);
        underlyingShape = sphereShape;
    }

    @Override
    public CollisionShape rotate(Quat4f rot) {
        return this;
    }

    @Override
    public float getRadius() {
        return sphereShape.getRadius();
    }
}

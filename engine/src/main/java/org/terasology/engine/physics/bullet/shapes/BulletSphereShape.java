// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.physics.bullet.shapes;

import com.badlogic.gdx.physics.bullet.collision.btSphereShape;
import org.joml.Quaternionf;
import org.terasology.engine.physics.shapes.CollisionShape;
import org.terasology.engine.physics.shapes.SphereShape;

public class BulletSphereShape extends BulletCollisionShape implements SphereShape {
    private final btSphereShape sphereShape;

    public BulletSphereShape(float radius) {
        sphereShape = new btSphereShape(radius);
        underlyingShape = sphereShape;
    }

    @Override
    public CollisionShape rotate(Quaternionf rot) {
        return this;
    }

    @Override
    public float getRadius() {
        return sphereShape.getRadius();
    }
}

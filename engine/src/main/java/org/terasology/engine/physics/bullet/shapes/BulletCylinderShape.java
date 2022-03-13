// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.physics.bullet.shapes;

import com.badlogic.gdx.physics.bullet.collision.btCylinderShape;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.terasology.engine.physics.shapes.CollisionShape;


public class BulletCylinderShape extends  BulletCollisionShape {

    private final btCylinderShape cylinderShape;

    public BulletCylinderShape(Vector3f halfExtents) {

        this.cylinderShape = new btCylinderShape(halfExtents);
        this.underlyingShape = cylinderShape;
    }

    @Override
    public CollisionShape rotate(Quaternionf rot) {
        return this;
    }
}

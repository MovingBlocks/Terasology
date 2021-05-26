// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.physics.bullet.shapes;

import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.terasology.engine.physics.shapes.BoxShape;
import org.terasology.engine.physics.shapes.CollisionShape;

public class BulletBoxShape extends BulletCollisionShape implements BoxShape {
    private final btBoxShape boxShape;

    public BulletBoxShape(Vector3f halfExtents) {
        boxShape = new btBoxShape(halfExtents);
        underlyingShape = boxShape;
    }

    @Override
    public CollisionShape rotate(Quaternionf rot) {
        Vector3f halfExtentsWithMargin = new Vector3f(boxShape.getHalfExtentsWithMargin());
        return new BulletBoxShape(halfExtentsWithMargin.rotate(rot).absolute());
    }

    @Override
    public Vector3f getHalfExtentsWithoutMargin() {
        return boxShape.getHalfExtentsWithoutMargin().mul(2);
    }
}

// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.physics.bullet.shapes;

import org.joml.Vector3f;
import org.terasology.engine.physics.shapes.BoxShape;
import org.terasology.engine.physics.shapes.CollisionShapeFactory;
import org.terasology.engine.physics.shapes.CompoundShape;
import org.terasology.engine.physics.shapes.ConvexHullShape;
import org.terasology.engine.physics.shapes.SphereShape;

import java.util.List;

public class BulletCollisionShapeFactory implements CollisionShapeFactory {
    @Override
    public BoxShape getNewBox(Vector3f extents) {
        return new BulletBoxShape(extents.mul(0.5f));
    }

    @Override
    public ConvexHullShape getNewConvexHull(List<Vector3f> vertices) {
        return new BulletConvexHullShape(vertices);
    }

    @Override
    public CompoundShape getNewCompoundShape() {
        return new BulletCompoundShape();
    }

    @Override
    public SphereShape getNewSphere(float radius) {
        return new BulletSphereShape(radius);
    }
}

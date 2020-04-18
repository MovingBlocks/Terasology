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

import org.joml.Vector3f;
import org.terasology.physics.shapes.BoxShape;
import org.terasology.physics.shapes.CollisionShapeFactory;
import org.terasology.physics.shapes.CompoundShape;
import org.terasology.physics.shapes.ConvexHullShape;
import org.terasology.physics.shapes.SphereShape;

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

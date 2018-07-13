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

import gnu.trove.iterator.TFloatIterator;
import org.lwjgl.BufferUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.characters.CharacterMovementComponent;
import org.terasology.math.geom.Vector3f;
import org.terasology.physics.components.shapes.BoxShapeComponent;
import org.terasology.physics.components.shapes.CapsuleShapeComponent;
import org.terasology.physics.components.shapes.CylinderShapeComponent;
import org.terasology.physics.components.shapes.HullShapeComponent;
import org.terasology.physics.components.shapes.SphereShapeComponent;
import org.terasology.physics.shapes.BoxShape;
import org.terasology.physics.shapes.CollisionShapeFactory;
import org.terasology.physics.shapes.CompoundShape;
import org.terasology.physics.shapes.ConvexHullShape;
import org.terasology.physics.shapes.SphereShape;

import java.nio.FloatBuffer;
import java.util.List;

public class BulletCollisionShapeFactory implements CollisionShapeFactory {
    private static final Logger logger = LoggerFactory.getLogger(BulletCollisionShapeFactory.class);

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

    @Override
    public BulletCollisionShape getShapeFor(EntityRef entityRef) {
        BoxShapeComponent box = entityRef.getComponent(BoxShapeComponent.class);
        if (box != null) {
            Vector3f halfExtents = new Vector3f(box.extents);
            return new BulletBoxShape(halfExtents.mul(.5f));
        }
        SphereShapeComponent sphere = entityRef.getComponent(SphereShapeComponent.class);
        if (sphere != null) {
            return new BulletSphereShape(sphere.radius);
        }
        CapsuleShapeComponent capsule = entityRef.getComponent(CapsuleShapeComponent.class);
        if (capsule != null) {
            return new BulletCapsuleShape(capsule.radius,capsule.height);
        }
        CylinderShapeComponent cylinder = entityRef.getComponent(CylinderShapeComponent.class);
        if (cylinder != null) {
            return new BulletCylinderShape(new Vector3f(cylinder.radius, 0.5f * cylinder.height, cylinder.radius));
        }
        HullShapeComponent hull = entityRef.getComponent(HullShapeComponent.class);
        if (hull != null) {
            FloatBuffer buffer = BufferUtils.createFloatBuffer( hull.sourceMesh.getVertices().size());
            TFloatIterator iterator = hull.sourceMesh.getVertices().iterator();
            int numPoints = 0;
            while (iterator.hasNext()) {
                numPoints++;
                buffer.put(iterator.next());
                buffer.put(iterator.next());
                buffer.put(iterator.next());
            }
            return new BulletConvexHullShape(buffer,numPoints,3 * Float.BYTES);
        }
        CharacterMovementComponent characterMovementComponent = entityRef.getComponent(CharacterMovementComponent.class);
        if (characterMovementComponent != null) {
            return new BulletCapsuleShape(characterMovementComponent.radius,characterMovementComponent.height);
        }
        logger.error("Creating physics object that requires a ShapeComponent or CharacterMovementComponent, but has neither. Entity: {}", entityRef);
        throw new IllegalArgumentException("Creating physics object that requires a ShapeComponent or CharacterMovementComponent, but has neither. Entity: " + entityRef);
    }
}

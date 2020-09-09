// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.physics.bullet;

import com.bulletphysics.linearmath.MotionState;
import com.bulletphysics.linearmath.Transform;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.math.VecMath;

/**
 * This motion state is used to connect rigid body entities to their rigid body in the bullet physics engine.
 * Bullet reads the initial state of the rigid body out of the entity, and then updates its location and rotation
 * as it moves under physics.
 *
 */
public class EntityMotionState extends MotionState {
    private final EntityRef entity;

    /**
     * Only the BulletPhysics class is expected to create instances.
     *
     * @param entity The entity to relate this motion state to and set the
     *               LocationComponent of.
     */
    EntityMotionState(EntityRef entity) {
        this.entity = entity;
    }

    @Override
    public Transform getWorldTransform(Transform transform) {
        LocationComponent loc = entity.getComponent(LocationComponent.class);
        if (loc != null&& !Float.isNaN(loc.getWorldPosition().x)) {
            // NOTE: JBullet ignores scale anyway
            transform.set(new javax.vecmath.Matrix4f(VecMath.to(loc.getWorldRotation()), VecMath.to(loc.getWorldPosition()), 1));
        }
        return transform;
    }

    @Override
    public void setWorldTransform(Transform transform) {
        LocationComponent loc = entity.getComponent(LocationComponent.class);
        if (loc != null&& !Float.isNaN(loc.getWorldPosition().x)) {
            loc.setWorldPosition(VecMath.from(transform.origin));
            loc.setWorldRotation(VecMath.from(transform.getRotation(new javax.vecmath.Quat4f())));
        }
    }

}

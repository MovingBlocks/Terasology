/*
 * Copyright 2013 MovingBlocks
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

package org.terasology.physics.bullet;

import com.bulletphysics.linearmath.MotionState;
import com.bulletphysics.linearmath.Transform;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.VecMath;

/**
 * This motion state is used to connect rigid body entities to their rigid body in the bullet physics engine.
 * Bullet reads the initial state of the rigid body out of the entity, and then updates its location and rotation
 * as it moves under physics.
 *
 */
public class EntityMotionState extends MotionState {
    private EntityRef entity;

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
        if (loc != null) {
            // NOTE: JBullet ignores scale anyway
            transform.set(new javax.vecmath.Matrix4f(VecMath.to(loc.getWorldRotation()), VecMath.to(loc.getWorldPosition()), 1));
        }
        return transform;
    }

    @Override
    public void setWorldTransform(Transform transform) {
        LocationComponent loc = entity.getComponent(LocationComponent.class);
        if (loc != null) {
            loc.setWorldPosition(VecMath.from(transform.origin));
            loc.setWorldRotation(VecMath.from(transform.getRotation(new javax.vecmath.Quat4f())));
        }
    }

}

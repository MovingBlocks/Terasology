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

import com.badlogic.gdx.physics.bullet.linearmath.btMotionState;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.location.LocationComponent;

/**
 * This motion state is used to connect rigid body entities to their rigid body in the bullet physics engine.
 * Bullet reads the initial state of the rigid body out of the entity, and then updates its location and rotation
 * as it moves under physics.
 *
 */
public class EntityMotionState extends btMotionState {
    private static final Logger logger = LoggerFactory.getLogger(EntityMotionState.class);

    private EntityRef entity;
    private Quaternionf rot = new Quaternionf();
    private Vector3f position = new Vector3f();

    /**
     * Only the BulletPhysics class is expected to create instances.
     *
     * @param entity The entity to relate this motion state to and set the LocationComponent of.
     */
    EntityMotionState(EntityRef entity) {
        super();
        this.entity = entity;
    }

    @Override
    public void getWorldTransform(Matrix4f transform) {
        LocationComponent loc = entity.getComponent(LocationComponent.class);
        transform.translationRotateScale(loc.getWorldPosition(position),
            loc.getWorldRotation(rot), loc.getWorldScale());
    }

    @Override
    public void setWorldTransform(Matrix4f transform) {
        LocationComponent loc = entity.getComponent(LocationComponent.class);
        if (loc != null) {
            rot.setFromNormalized(transform);
            rot.normalize();
            transform.getTranslation(position);

            loc.setWorldRotation(rot);
            loc.setWorldPosition(position);
        }
    }
}

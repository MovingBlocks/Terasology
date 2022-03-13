// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.physics.bullet;

import com.badlogic.gdx.physics.bullet.linearmath.btMotionState;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.location.LocationComponent;

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

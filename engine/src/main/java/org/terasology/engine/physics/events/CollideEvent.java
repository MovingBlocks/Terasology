// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.physics.events;

import org.joml.Vector3f;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.AbstractConsumableEvent;

/**
 * TODO Make CollideEvent as a server event.
 * TODO Have a CollideRequest before the CollideEvent?
 */
public class CollideEvent extends AbstractConsumableEvent {
    private EntityRef otherEntity = EntityRef.NULL;
    private Vector3f entityContactPoint = new Vector3f();
    private Vector3f otherEntityContactPoint = new Vector3f();
    private float penetration = 0.0f;
    private Vector3f normal = new Vector3f();

    public CollideEvent(EntityRef other, Vector3f entityContactPoint, Vector3f otherEntityContactPoint, float penetration, Vector3f normal) {
        this.otherEntity = other;
        this.normal.set(normal);
        this.entityContactPoint.set(entityContactPoint);
        this.otherEntityContactPoint.set(otherEntityContactPoint);
        this.penetration = penetration;
    }

    public Vector3f getNormal() {
        return normal;
    }

    public Vector3f getEntityContactPoint() {
        return entityContactPoint;
    }

    public Vector3f getOtherEntityContactPoint() {
        return otherEntityContactPoint;
    }

    public float getPenetration() {
        return penetration;
    }

    public EntityRef getOtherEntity() {
        return otherEntity;
    }
}

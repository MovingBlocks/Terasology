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
    private EntityRef otherEntity;
    private Vector3f entityContactPoint;
    private Vector3f otherEntityContactPoint;
    private float penetration;
    private Vector3f normal;

    public CollideEvent(EntityRef other, Vector3f entityContactPoint, Vector3f otherEntityContactPoint, float penetration, Vector3f normal) {
        this.otherEntity = other;
        this.normal = normal;
        this.entityContactPoint = entityContactPoint;
        this.otherEntityContactPoint = otherEntityContactPoint;
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

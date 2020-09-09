// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.physics.events;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.AbstractConsumableEvent;
import org.terasology.math.geom.Vector3f;

/**
 * TODO Make CollideEvent as a server event.
 * TODO Have a CollideRequest before the CollideEvent?
 */
public class CollideEvent extends AbstractConsumableEvent {
    private final EntityRef otherEntity;
    private final Vector3f entityContactPoint;
    private final Vector3f otherEntityContactPoint;
    private final float penetration;
    private final Vector3f normal;

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

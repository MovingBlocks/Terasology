// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.physics.events;

import org.joml.Vector3f;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.AbstractConsumableEvent;
import org.terasology.engine.math.Side;
import org.terasology.engine.network.BroadcastEvent;

/**
 * Impact event is called whenever an item has enough speed to detect
 * penetration of a block or entity in the next frame. It computes the
 * reflection angle, speed and the next position the item should be in.
 */
@BroadcastEvent
public class ImpactEvent extends AbstractConsumableEvent {
    private Vector3f impactPoint;
    private Vector3f impactNormal;
    private Vector3f impactSpeed;
    private float travelDistance;
    private EntityRef impactEntity;

    protected ImpactEvent() {
    }

    public ImpactEvent(Vector3f impactPoint, Vector3f impactNormal, Vector3f impactSpeed, float travelDistance, EntityRef impactEntity) {
        this.impactPoint = impactPoint;
        this.impactNormal = impactNormal;
        this.impactSpeed = impactSpeed;
        this.travelDistance = travelDistance;
        this.impactEntity = impactEntity;
    }

    public Vector3f getImpactPoint() {
        return impactPoint;
    }

    public Vector3f getImpactNormal() {
        return impactNormal;
    }

    public Vector3f getImpactSpeed() {
        return impactSpeed;
    }

    public float getTravelDistance() {
        return travelDistance;
    }

    public EntityRef getImpactEntity() {
        return impactEntity;
    }

    public Side getSide() {
        return Side.inDirection(impactNormal);
    }
}

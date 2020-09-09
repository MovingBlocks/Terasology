// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.common;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.AbstractConsumableEvent;
import org.terasology.engine.logic.characters.events.ActivationRequest;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.math.geom.Vector3f;

/**
 *
 */
// TODO: This should not be consumable. Instead have a consumable BeforeActivate event to allow cancellation
public class ActivateEvent extends AbstractConsumableEvent {
    private final EntityRef instigator;
    private final EntityRef target;
    private final Vector3f origin;
    private final Vector3f direction;
    private final Vector3f hitPosition;
    private final Vector3f hitNormal;
    private final int activationId;

    public ActivateEvent(EntityRef target, EntityRef instigator, Vector3f origin, Vector3f direction,
                         Vector3f hitPosition, Vector3f hitNormal, int activationId) {
        this.instigator = instigator;
        this.target = target;
        this.direction = direction;
        this.hitPosition = hitPosition;
        this.hitNormal = hitNormal;
        this.origin = origin;
        this.activationId = activationId;
    }

    public ActivateEvent(ActivationRequest event) {
        this.instigator = event.getInstigator();
        this.target = event.getTarget();
        this.direction = event.getDirection();
        this.hitPosition = event.getHitPosition();
        this.hitNormal = event.getHitNormal();
        this.origin = event.getOrigin();
        this.activationId = event.getActivationId();
    }

    public EntityRef getInstigator() {
        return instigator;
    }

    public EntityRef getTarget() {
        return target;
    }

    public Vector3f getOrigin() {
        return origin;
    }

    public Vector3f getDirection() {
        return direction;
    }

    public Vector3f getHitPosition() {
        return hitPosition;
    }

    public Vector3f getHitNormal() {
        return hitNormal;
    }

    public int getActivationId() {
        return activationId;
    }

    public Vector3f getTargetLocation() {
        LocationComponent loc = target.getComponent(LocationComponent.class);
        if (loc != null && !Float.isNaN(loc.getWorldPosition().x)) {
            return loc.getWorldPosition();
        }
        return null;
    }

    public Vector3f getInstigatorLocation() {
        LocationComponent loc = instigator.getComponent(LocationComponent.class);
        if (loc != null && !Float.isNaN(loc.getWorldPosition().x)) {
            return loc.getWorldPosition();
        }
        return new Vector3f();
    }
}

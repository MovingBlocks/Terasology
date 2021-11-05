// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.common;

import org.joml.Vector3f;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.AbstractConsumableEvent;
import org.terasology.engine.logic.characters.events.ActivationRequest;
import org.terasology.engine.logic.location.LocationComponent;

// TODO: This should not be consumable. Instead have a consumable BeforeActivate event to allow cancellation
public class ActivateEvent extends AbstractConsumableEvent {
    private EntityRef instigator;
    private EntityRef target;
    private Vector3f origin;
    private Vector3f direction;
    private Vector3f hitPosition;
    private Vector3f hitNormal;
    private int activationId;

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

    /**
     * @return the entity that is targeted, or {@link EntityRef#NULL} if the activation has no target
     */
    public EntityRef getTarget() {
        return target;
    }

    public Vector3f getOrigin() {
        return origin;
    }

    public Vector3f getDirection() {
        return direction;
    }

    /**
     * @return the hit position if {@link #getTarget()} exists, {@code null} otherwise
     */
    public Vector3f getHitPosition() {
        return hitPosition;
    }

    /**
     * @return the hit normal if {@link #getTarget()} exists, {@code null} otherwise
     */
    public Vector3f getHitNormal() {
        return hitNormal;
    }

    public int getActivationId() {
        return activationId;
    }

    /**
     * @return the target location if {@link #getTarget()} exists and has a location, {@code null} otherwise
     */
    public Vector3f getTargetLocation() {
        LocationComponent loc = target.getComponent(LocationComponent.class);
        if (loc != null) {
            Vector3f result = loc.getWorldPosition(new Vector3f());
            if (result.isFinite()) {
                return result;
            }
        }
        return null;
    }

    public Vector3f getInstigatorLocation() {
        LocationComponent loc = instigator.getComponent(LocationComponent.class);
        if (loc != null) {
            Vector3f result = loc.getWorldPosition(new Vector3f());
            if (result.isFinite()) {
                return result;
            }
            result.set(0, 0, 0);
            return result;
        }
        return new Vector3f();
    }

    @Override
    public String toString() {
        return "ActivateEvent{" +
                "instigator=" + instigator +
                ", target=" + target +
                ", origin=" + origin +
                ", direction=" + direction +
                ", hitPosition=" + hitPosition +
                ", hitNormal=" + hitNormal +
                ", activationId=" + activationId +
                '}';
    }
}

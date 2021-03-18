// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.input.events;

import org.joml.Vector3fc;
import org.joml.Vector3ic;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ConsumableEvent;


public abstract class InputEvent implements ConsumableEvent {
    private float delta;
    private boolean consumed;

    private EntityRef target = EntityRef.NULL;
    private Vector3ic targetBlockPosition;
    private Vector3fc hitPosition;
    private Vector3fc hitNormal;

    public InputEvent(float delta) {
        this.delta = delta;
    }

    public EntityRef getTarget() {
        return target;
    }

    /**
     *
     * @return This event's target world position.
     */
    public Vector3fc getHitPosition() {
        return hitPosition;
    }

    /**
     *
     * @return The hit normal/direction of this event (usually from player camera).
     */
    public Vector3fc getHitNormal() {
        return hitNormal;
    }

    public Vector3ic getTargetBlockPosition() {
        return targetBlockPosition;
    }

    /**
     *
     * @return The time since the event was fired (also the game update loop's delta time).
     */
    public float getDelta() {
        return delta;
    }

    @Override
    public boolean isConsumed() {
        return consumed;
    }

    @Override
    public void consume() {
        this.consumed = true;
    }

    /**
     * Sets this event's target information.
     * @param newTarget This event's target entity.
     * @param targetBlockPos This event's target block coordinates.
     * @param targetHitPosition This event's target world position.
     * @param targetHitNormal The hit normal/direction of this event (usually from player camera).
     */
    public void setTargetInfo(EntityRef newTarget, Vector3ic targetBlockPos, Vector3fc targetHitPosition, Vector3fc targetHitNormal) {
        this.target = newTarget;
        this.targetBlockPosition = targetBlockPos;
        this.hitPosition = targetHitPosition;
        this.hitNormal = targetHitNormal;
    }

    protected void reset(float newDelta) {
        consumed = false;
        this.delta = newDelta;
        this.target = EntityRef.NULL;
    }


}

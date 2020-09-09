// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.input.events;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ConsumableEvent;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;


public abstract class InputEvent implements ConsumableEvent {
    private float delta;
    private boolean consumed;

    private EntityRef target = EntityRef.NULL;
    private Vector3i targetBlockPosition;
    private Vector3f hitPosition;
    private Vector3f hitNormal;

    public InputEvent(float delta) {
        this.delta = delta;
    }

    public EntityRef getTarget() {
        return target;
    }

    /**
     * @return This event's target world position.
     */
    public Vector3f getHitPosition() {
        return hitPosition;
    }

    /**
     * @return The hit normal/direction of this event (usually from player camera).
     */
    public Vector3f getHitNormal() {
        return hitNormal;
    }

    public Vector3i getTargetBlockPosition() {
        return targetBlockPosition;
    }

    /**
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
     *
     * @param newTarget This event's target entity.
     * @param targetBlockPos This event's target block coordinates.
     * @param targetHitPosition This event's target world position.
     * @param targetHitNormal The hit normal/direction of this event (usually from player camera).
     */
    public void setTargetInfo(EntityRef newTarget, Vector3i targetBlockPos, Vector3f targetHitPosition,
                              Vector3f targetHitNormal) {
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

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
package org.terasology.input.events;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ConsumableEvent;
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

    public Vector3f getHitPosition() {
        return hitPosition;
    }

    public Vector3f getHitNormal() {
        return hitNormal;
    }

    public Vector3i getTargetBlockPosition() {
        return targetBlockPosition;
    }

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

    public void setTargetInfo(EntityRef newTarget, Vector3i targetBlockPos, Vector3f targetHitPosition, Vector3f targetHitNormal) {
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

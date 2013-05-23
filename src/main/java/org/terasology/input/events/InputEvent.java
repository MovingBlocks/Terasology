/*
 * Copyright 2013 Moving Blocks
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

import org.terasology.entitySystem.event.AbstractEvent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.math.Vector3i;

import javax.vecmath.Vector3f;


public abstract class InputEvent extends AbstractEvent {
    private boolean consumed;
    private float delta;

    private EntityRef target = EntityRef.NULL;
    private Vector3i targetBlockPosition;
    private Vector3f hitPosition;
    private Vector3f hitNormal;

    public InputEvent(float delta) {
        this.delta = delta;
    }

    public void setTarget(EntityRef target, Vector3i targetBlockPos, Vector3f hitPosition, Vector3f hitNormal) {
        this.target = target;
        this.targetBlockPosition = targetBlockPos;
        this.hitPosition = hitPosition;
        this.hitNormal = hitNormal;
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

    public void consume() {
        consumed = true;
        cancel();
    }

    public boolean isConsumed() {
        return consumed;
    }

    protected void reset(float delta) {
        consumed = false;
        cancelled = false;
        this.delta = delta;
        this.target = EntityRef.NULL;
    }
}

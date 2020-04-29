/*
 * Copyright 2020 MovingBlocks
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
package org.terasology.logic.characters.events;

import org.terasology.entitySystem.event.AbstractConsumableEvent;

/**
 * Mutable event to request that some character entity should be scaled to a target value.
 * <p>
 * This event may be consumed to cancel the scaling process. The target value can be altered by other systems.
 * <p>
 * To scale by a factor use {@link ScaleByRequest}.
 * <p>
 * Event handlers responsible for applying the scaling should not act on this event but listen for {@link OnScaleEvent}
 * instead.
 *
 * @see ScaleByRequest
 * @see OnScaleEvent
 */
public class ScaleToRequest extends AbstractConsumableEvent {
    private float targetValue;

    /**
     * Create a new request to scale to a target value.
     *
     * @param targetValue the target value - must be greater zero
     */
    public ScaleToRequest(float targetValue) {
        if (targetValue <= 0) {
            throw new IllegalArgumentException("zero or negative target height");
        }
        this.targetValue = targetValue;
    }

    /**
     * @return the target value - guaranteed to be greater zero
     */
    public float getTargetValue() {
        return targetValue;
    }

    /**
     * Change the target value of this request.
     *
     * @param targetValue the target value - must be greater zero
     */
    public void setTargetValue(float targetValue) {
        if (targetValue <= 0) {
            throw new IllegalArgumentException("zero or negative target height");
        }
        this.targetValue = targetValue;
    }
}

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
 * Mutable event to request that some character entity should be scaled by the given factor.
 * <p>
 * This event may be consumed to cancel the scaling process. The scaling factor can be altered by other systems.
 * <p>
 * To scale to an absolute target value use {@link ScaleToRequest}.
 * <p>
 * Event handlers responsible for applying the scaling should not act on this event but listen for {@link OnScaleEvent}
 * instead.
 *
 * @see ScaleToRequest
 * @see OnScaleEvent
 */
public class ScaleByRequest extends AbstractConsumableEvent {
    private float factor;

    /**
     * Create a new request to scale by factor.
     *
     * @param factor the scaling factor - must be greater zero
     */
    public ScaleByRequest(float factor) {
        if (factor <= 0) {
            throw new IllegalArgumentException("zero or negative factor");
        }
        this.factor = factor;
    }

    /**
     * @return the scaling factor - guaranteed to be greater zero
     */
    public float getFactor() {
        return factor;
    }

    /**
     * Change the scaling factor of this request.
     *
     * @param factor the new factor - must be greater zero
     */
    public void setFactor(float factor) {
        if (factor <= 0) {
            throw new IllegalArgumentException("zero or negative factor");
        }
        this.factor = factor;
    }
}

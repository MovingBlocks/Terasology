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

import org.terasology.entitySystem.event.Event;

/**
 * Immutable event to notify that a character entity is scaled.
 * <p>
 * System should react on this to update logic or components according to the new size.
 * Receiving this event denotes that the scaling was "accepted" and should now be performed by systems.
 * <p>
 * Event handlers may not assume that all components of the entity are already updated to be consistent with the new
 * value but should ideally only rely on the information given by the event.
 */
public class OnScaleEvent implements Event {

    private final float newValue;
    private final float oldValue;

    /**
     * Create a new notification event on character scaling.
     *
     * @param oldValue the entity's old height
     * @param newValue the entity's new height (must be greater zero)
     */
    public OnScaleEvent(final float oldValue, final float newValue) {
        if (newValue <= 0) {
            throw new IllegalArgumentException("zero or negative value");
        }
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    /**
     * The old entity height previous to scaling.
     */
    public float getOldValue() {
        return oldValue;
    }

    /**
     * The new entity height to scale to.
     */
    public float getNewValue() {
        return newValue;
    }

    /**
     * The scaling factor determined by the quotient of new and old value.
     * <p>
     * This is guaranteed to be greater zero (> 0).
     */
    public float getFactor() {
        return newValue / oldValue;
    }
}

// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.characters.events;

import org.terasology.gestalt.entitysystem.event.Event;

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
     * This is guaranteed to be greater than 0.
     */
    public float getFactor() {
        return newValue / oldValue;
    }
}

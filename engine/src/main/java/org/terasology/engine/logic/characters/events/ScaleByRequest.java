// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.characters.events;

import org.terasology.engine.entitySystem.event.AbstractConsumableEvent;

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

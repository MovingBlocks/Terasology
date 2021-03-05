// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.characters.events;

import org.terasology.engine.entitySystem.event.AbstractConsumableEvent;

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

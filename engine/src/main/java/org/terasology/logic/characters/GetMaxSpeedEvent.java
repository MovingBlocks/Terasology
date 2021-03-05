// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.characters;

import org.terasology.engine.entitySystem.event.AbstractValueModifiableEvent;

public class GetMaxSpeedEvent extends AbstractValueModifiableEvent {
    private MovementMode movementMode;

    public GetMaxSpeedEvent(float baseValue, MovementMode movementMode) {
        super(baseValue);
        this.movementMode = movementMode;
    }

    public MovementMode getMovementMode() {
        return movementMode;
    }
}

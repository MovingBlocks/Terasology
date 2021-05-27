// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.characters.events;

import org.terasology.engine.entitySystem.event.Event;
import org.terasology.engine.logic.characters.MovementMode;

public class SetMovementModeEvent implements Event {
    private MovementMode mode;

    public SetMovementModeEvent(MovementMode mode) {
        this.mode = mode;
    }

    public MovementMode getMode() {
        return mode;
    }
}

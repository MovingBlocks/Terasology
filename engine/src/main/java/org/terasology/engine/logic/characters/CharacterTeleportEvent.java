// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.characters;

import org.joml.Vector3fc;
import org.terasology.gestalt.entitysystem.event.Event;

/**
 * Used within the server to trigger a teleport of a character. Just chaining the position is not possible due to
 * movement prediction.
 */
public class CharacterTeleportEvent implements Event {
    private Vector3fc targetPosition;

    public CharacterTeleportEvent(Vector3fc targetPosition) {
        this.targetPosition = targetPosition;
    }

    public Vector3fc getTargetPosition() {
        return targetPosition;
    }
}

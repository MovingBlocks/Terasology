// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.characters;

import org.terasology.engine.entitySystem.event.Event;
import org.terasology.math.geom.Vector3f;

/**
 * Used within the server to trigger a teleport of a character. Just chaining the position is not possible due to
 * movement prediction.
 */
public class CharacterTeleportEvent implements Event {
    private final Vector3f targetPosition;

    public CharacterTeleportEvent(Vector3f targetPosition) {
        this.targetPosition = targetPosition;
    }

    public Vector3f getTargetPosition() {
        return targetPosition;
    }
}

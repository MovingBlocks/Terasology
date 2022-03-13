// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.characters;

import org.joml.Vector3f;
import org.terasology.gestalt.entitysystem.event.Event;

public class CharacterImpulseEvent implements Event {
    Vector3f direction;

    public CharacterImpulseEvent(Vector3f direction) {
        this.direction = direction;
    }

    public Vector3f getDirection() {
        return direction;
    }
}

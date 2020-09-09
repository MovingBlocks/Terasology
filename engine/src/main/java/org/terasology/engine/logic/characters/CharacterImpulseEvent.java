// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.characters;

import org.terasology.engine.entitySystem.event.Event;
import org.terasology.math.geom.Vector3f;

public class CharacterImpulseEvent implements Event {
    Vector3f direction;

    public CharacterImpulseEvent(Vector3f direction) {
        this.direction = direction;
    }

    public Vector3f getDirection() {
        return direction;
    }
}

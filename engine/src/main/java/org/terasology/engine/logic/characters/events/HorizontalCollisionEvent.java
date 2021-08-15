// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.characters.events;

import org.joml.Vector3f;

public class HorizontalCollisionEvent extends CollisionEvent {

    public HorizontalCollisionEvent(Vector3f location, Vector3f velocity) {
        super(location, velocity);
    }

}

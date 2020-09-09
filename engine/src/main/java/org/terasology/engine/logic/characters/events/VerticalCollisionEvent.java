// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.characters.events;

import org.terasology.math.geom.Vector3f;

/**
 *
 */
public class VerticalCollisionEvent extends CollisionEvent {

    public VerticalCollisionEvent(Vector3f location, Vector3f velocity) {
        super(location, velocity);
    }
}

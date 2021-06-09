// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.players;

import org.joml.Vector3f;
import org.terasology.engine.network.Replicate;
import org.terasology.gestalt.entitysystem.component.Component;

/**
 * This is attached to the player entities in order to manually set a custom spawn location.
 */
public class StaticSpawnLocationComponent implements Component<StaticSpawnLocationComponent> {
    @Replicate
    public Vector3f position;

    @Override
    public void copy(StaticSpawnLocationComponent other) {
        this.position = new Vector3f(other.position);
    }
}

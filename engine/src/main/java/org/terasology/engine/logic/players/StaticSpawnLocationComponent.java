// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.players;

import org.joml.Vector3f;
import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.network.Replicate;

/**
 * This is attached to the player entities in order to manually set a custom spawn location.
 */
public class StaticSpawnLocationComponent implements Component {
    @Replicate
    public Vector3f position;
}

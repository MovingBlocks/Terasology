// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.ai;

import org.joml.Vector3f;
import org.terasology.gestalt.entitysystem.component.Component;

public final class SimpleAIComponent implements Component<SimpleAIComponent> {

    public long lastChangeOfDirectionAt;
    public Vector3f movementTarget = new Vector3f();
    public boolean followingPlayer;

    @Override
    public void copy(SimpleAIComponent other) {
        this.lastChangeOfDirectionAt = other.lastChangeOfDirectionAt;
        this.movementTarget = new Vector3f(other.movementTarget);
        this.followingPlayer = other.followingPlayer;
    }
}

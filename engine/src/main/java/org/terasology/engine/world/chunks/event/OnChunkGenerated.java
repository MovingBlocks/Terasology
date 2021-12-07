// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.chunks.event;

import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.terasology.gestalt.entitysystem.event.Event;

public class OnChunkGenerated implements Event {

    private Vector3i chunkPos = new Vector3i();

    public OnChunkGenerated() {
    }

    public OnChunkGenerated(Vector3ic chunkPos) {
        this.chunkPos.set(chunkPos);
    }

    public Vector3ic getChunkPos() {
        return chunkPos;
    }
}

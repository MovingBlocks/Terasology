// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.chunks.event;

import org.terasology.engine.entitySystem.event.Event;
import org.terasology.math.geom.Vector3i;

/**
 *
 */
public class OnChunkLoaded implements Event {
    private final Vector3i chunkPos = new Vector3i();

    public OnChunkLoaded(Vector3i chunkPos) {
        this.chunkPos.set(chunkPos);
    }

    public Vector3i getChunkPos() {
        return chunkPos;
    }
}

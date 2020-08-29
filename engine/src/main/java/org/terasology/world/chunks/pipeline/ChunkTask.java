// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.world.chunks.pipeline;

import org.joml.Vector3i;
import org.terasology.utilities.concurrency.Task;
import org.terasology.world.chunks.Chunk;

/**
 *
 */
public interface ChunkTask extends Task {

    Chunk getChunk();

    default Vector3i getPosition() {
        return getChunk().getPosition(new Vector3i());
    }

    default boolean needsRepeat() {
        return false;
    }

}

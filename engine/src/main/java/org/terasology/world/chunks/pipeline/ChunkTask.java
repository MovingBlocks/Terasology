// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.world.chunks.pipeline;

import org.joml.Vector3i;
import org.terasology.utilities.concurrency.Task;
import org.terasology.world.chunks.Chunk;

public interface ChunkTask extends Task {

    Chunk getChunk();

    default Vector3i getPosition() {
        return new Vector3i();
    }
}

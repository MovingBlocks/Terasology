// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.world.chunks.pipeline.tasks;

import org.terasology.world.chunks.Chunk;

@FunctionalInterface
public interface ChunkPipelineTaskListener {

    void fire(Chunk chunk);
}

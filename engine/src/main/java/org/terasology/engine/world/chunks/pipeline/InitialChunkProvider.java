// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.chunks.pipeline;

import org.joml.Vector3ic;
import org.terasology.engine.world.chunks.Chunk;

import java.util.Optional;
import java.util.Set;

/**
 * An InitialChunkProvider provides input chunks to the ChunkProcessingPipeline.
 * All methods should be able to be called by multiple threads simultaneously, taking care of any necessary synchronization themselves.
 */
public interface InitialChunkProvider {
    /**
     * @param currentlyGenerating the set of chunks which are currently being processed.
     *  This lets the InitialChunkProvider discard those chunks before trying to generate them at all.
     */
    Optional<Chunk> next(Set<Vector3ic> currentlyGenerating);
}

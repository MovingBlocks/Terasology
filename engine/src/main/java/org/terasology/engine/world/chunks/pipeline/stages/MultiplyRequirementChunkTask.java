// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.chunks.pipeline.stages;

import org.joml.Vector3ic;
import org.terasology.engine.world.chunks.Chunk;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * Chunk task which require many chunks for processing.
 */
public class MultiplyRequirementChunkTask implements ChunkTask {
    private final String name;
    private final Vector3ic position;
    private final Function<Collection<Chunk>, Chunk> function;
    private final List<Vector3ic> requirements;

    public MultiplyRequirementChunkTask(String name, Vector3ic position, Function<Collection<Chunk>, Chunk> function,
                                        List<Vector3ic> requirements) {
        this.name = name;
        this.position = position;
        this.function = function;
        this.requirements = requirements;
    }

    @Override
    public List<Vector3ic> getRequirements() {
        return requirements;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Vector3ic getPosition() {
        return position;
    }

    @Override
    public Chunk apply(Collection<Chunk> chunks) {
        return function.apply(chunks);
    }
}

// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.world.chunks.pipeline.stages;

import com.google.common.base.Preconditions;
import org.joml.Vector3ic;
import org.terasology.world.chunks.Chunk;

import java.util.Collection;
import java.util.Optional;
import java.util.function.UnaryOperator;

/**
 * ChunkTask which required One chunk for processing.
 */
public class SingleChunkTask implements ChunkTask {
    private final String name;
    private final Vector3ic position;
    private final UnaryOperator<Chunk> function;

    public SingleChunkTask(String name, Vector3ic position, UnaryOperator<Chunk> function) {
        this.name = name;
        this.position = position;
        this.function = function;
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
        Preconditions.checkArgument(chunks.size() == 1, "SingleChunkTask must have only one chunk on input");
        Optional<Chunk> chunk = chunks.stream().findFirst();
        Preconditions.checkArgument(chunk.isPresent(), "SingleChunkTask must have chunk on input");
        return function.apply(chunk.get());
    }
}

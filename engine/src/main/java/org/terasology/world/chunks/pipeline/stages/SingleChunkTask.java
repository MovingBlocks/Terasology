// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.world.chunks.pipeline.stages;

import org.joml.Vector3ic;
import org.terasology.world.chunks.Chunk;

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
    public Chunk apply(Chunk chunk) {
        return function.apply(chunk);
    }
}

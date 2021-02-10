// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.world.chunks.pipeline.stages;

import org.joml.Vector3ic;
import org.terasology.world.chunks.Chunk;

import java.util.Collection;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * Provides ChunkTask for {@link org.terasology.world.chunks.pipeline.ChunkProcessingPipeline}
 * <p>
 * Also
 * <p>
 * Provides factory methods for creating ChunkTaskProviders.
 */
public class ChunkTaskProvider {
    private final String name;
    private final Function<Vector3ic, ChunkTask> taskCreator;

    public ChunkTaskProvider(String name, Function<Vector3ic, ChunkTask> taskCreator) {
        this.name = name;
        this.taskCreator = taskCreator;
    }

    public static ChunkTaskProvider create(String name, UnaryOperator<Chunk> processingFunction) {
        return new ChunkTaskProvider(
                name,
                pos -> new SingleChunkTask(name, pos, processingFunction));
    }

    public static ChunkTaskProvider create(String name, Consumer<Chunk> processingFunction) {
        return new ChunkTaskProvider(
                name,
                pos -> new SingleChunkTask(name, pos, (c) -> {
                    processingFunction.accept(c);
                    return c;
                }));
    }

    public static ChunkTaskProvider createMulti(String name, Function<Collection<Chunk>, Chunk> processing,
                                                Function<Vector3ic, Set<Vector3ic>> requirementCalculator) {
        return new ChunkTaskProvider(
                name,
                pos -> new MultiplyRequirementChunkTask(name, pos, processing, requirementCalculator.apply(pos))
        );
    }

    public String getName() {
        return name;
    }

    public ChunkTask createChunkTask(Vector3ic pos) {
        return taskCreator.apply(pos);
    }
}

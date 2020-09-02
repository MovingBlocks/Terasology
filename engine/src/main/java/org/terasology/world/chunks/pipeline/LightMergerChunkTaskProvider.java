// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.world.chunks.pipeline;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector3i;
import org.terasology.math.JomlUtil;
import org.terasology.world.block.BlockRegion;
import org.terasology.world.block.BlockRegionIterable;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.internal.GeneratingChunkProvider;
import org.terasology.world.chunks.pipeline.tasks.InterruptChunkTask;
import org.terasology.world.chunks.pipeline.tasks.LightMergerChunkTask;
import org.terasology.world.propagation.light.LightMerger;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Light Merger Chunk Task Provider.
 * Check requirements and re-invoke {@link LightMergerChunkTask} when requirements meets.
 */
public class LightMergerChunkTaskProvider implements Function<Chunk, ChunkTask>, ChunkTaskListener {

    private final GeneratingChunkProvider chunkProvider;
    private final LightMerger lightMerger;
    private final ChunkProcessingPipeline pipeline;
    private final Map<Chunk, Set<Vector3i>> requirements = new ConcurrentHashMap<>();

    public LightMergerChunkTaskProvider(GeneratingChunkProvider chunkProvider, LightMerger lightMerger,
                                        ChunkProcessingPipeline pipeline) {
        this.chunkProvider = chunkProvider;
        this.lightMerger = lightMerger;
        this.pipeline = pipeline;
    }

    @Override
    public ChunkTask apply(Chunk chunk) {
        Set<Vector3i> unloaded = getNearestUnloadedChunkPosition(chunk).collect(Collectors.toSet());
        if (unloaded.isEmpty()) {
            return makeTask(chunk);
        } else {
            requirements.put(chunk, unloaded);
            return new InterruptChunkTask();
        }
    }

    @Override
    public void onDone(ChunkTask chunkTask) {
        Set<Chunk> toInvoke = new HashSet<>();
        for (Map.Entry<Chunk, Set<Vector3i>> entry : requirements.entrySet()) {
            if (entry.getValue().remove(chunkTask.getPosition())) {
                if (entry.getValue().isEmpty()) {
                    toInvoke.add(entry.getKey());
                }
            }
            //  Set<Vector3i> unloaded = getNearestUnloadedChunkPosition(entry.getKey()).collect(Collectors.toSet());
            //            if (unloaded.isEmpty()) {
            //                toInvoke.add(entry.getKey());
            //            }
        }
        for (Chunk chunk : toInvoke) {
            requirements.remove(chunk);
            pipeline.doTaskWrapper(makeTask(chunk));
        }
    }

    @NotNull
    private LightMergerChunkTask makeTask(Chunk chunk) {
        return new LightMergerChunkTask(chunk, chunkProvider, lightMerger);
    }

    private Stream<Vector3i> getNearestUnloadedChunkPosition(Chunk chunk) {
        Vector3i chunkPosition = new Vector3i();
        chunk.getPosition(chunkPosition);
        BlockRegion extentsRegion = new BlockRegion(
                chunkPosition.x - 1, chunkPosition.y - 1, chunkPosition.z - 1,
                chunkPosition.x + 1, chunkPosition.y + 1, chunkPosition.z + 1);
        return StreamSupport.stream(BlockRegionIterable
                        .region(extentsRegion)
                        .build()
                        .spliterator(),
                false)
                .map(Vector3i::new)
                .filter((v) -> chunkProvider.getChunkUnready(JomlUtil.from(v)) == null); //TODO make chunkProvider
        // Joml ready.
    }
}

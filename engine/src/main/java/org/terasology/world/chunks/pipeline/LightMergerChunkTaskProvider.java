// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.world.chunks.pipeline;

import com.google.common.collect.Sets;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3i;
import org.terasology.math.JomlUtil;
import org.terasology.world.block.BlockRegion;
import org.terasology.world.block.BlockRegionIterable;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.ChunkProvider;
import org.terasology.world.chunks.pipeline.tasks.InterruptChunkTask;
import org.terasology.world.chunks.pipeline.tasks.LightMergerChunkTask;
import org.terasology.world.propagation.light.LightMerger;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Light Merger Chunk Task Provider. Check requirements and re-invoke {@link LightMergerChunkTask} when requirements
 * meets.
 */
public class LightMergerChunkTaskProvider implements Function<Chunk, ChunkTask>, ChunkTaskListener,
        ChunkRemoveFromPipelineListener {

    private final ChunkProvider chunkProvider;
    private final ChunkProcessingPipeline pipeline;
    private final Set<Chunk> pending = Sets.newConcurrentHashSet();
    private final Map<Vector3i, Chunk> noticedChunkInProcessing = new ConcurrentHashMap<>();

    public LightMergerChunkTaskProvider(ChunkProvider chunkProvider,
                                        ChunkProcessingPipeline pipeline) {
        this.chunkProvider = chunkProvider;
        this.pipeline = pipeline;
    }

    @Override
    public ChunkTask apply(Chunk chunk) {
        if (getLocalChunks(chunk).noneMatch(Objects::isNull)) {
            return makeTask(chunk, getLocalChunks(chunk).toArray(Chunk[]::new));
        } else {
            pending.add(chunk);
            return new InterruptChunkTask();
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param chunkTask ChunkTask which done processing.
     */
    @Override
    public void onDone(ChunkTask chunkTask) {
        if (!(chunkTask instanceof InterruptChunkTask)) {
            noticedChunkInProcessing.put(chunkTask.getPosition(), chunkTask.getChunk());
        }
        pending.removeIf(chunk -> {
            if (getLocalChunks(chunk).noneMatch(Objects::isNull)) {
                pipeline.doTaskWrapper(makeTask(chunk, getLocalChunks(chunk).toArray(Chunk[]::new)));
                return true;
            }
            return false;
        });
    }

    @NotNull
    private LightMergerChunkTask makeTask(Chunk chunk, Chunk[] localChunks) {
        return new LightMergerChunkTask(chunk, localChunks, new LightMerger());
    }

    private Stream<Chunk> getLocalChunks(Chunk chunk) {
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
                .map(this::getChunk);
    }

    /**
     * {@inheritDoc}
     *
     * @param pos position of chunk
     */
    @Override
    public void onRemove(Vector3i pos) {
        noticedChunkInProcessing.remove(pos);
    }

    private Chunk getChunk(Vector3i pos) {
        Chunk chunk = chunkProvider.getChunk(JomlUtil.from(pos));
        if (chunk == null) {
            chunk = noticedChunkInProcessing.get(pos);
        }
        return chunk;
    }

}

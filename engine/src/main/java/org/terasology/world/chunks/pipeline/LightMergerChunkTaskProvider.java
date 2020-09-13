// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.world.chunks.pipeline;

import com.google.common.collect.Maps;
import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.SettableFuture;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3i;
import org.terasology.math.JomlUtil;
import org.terasology.world.block.BlockRegion;
import org.terasology.world.block.BlockRegionIterable;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.ChunkProvider;
import org.terasology.world.chunks.pipeline.tasks.LightMergerChunkTask;
import org.terasology.world.propagation.light.LightMerger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Light Merger Chunk Task Provider.
 * <p>
 * Check requirements and re-invoke {@link LightMergerChunkTask} when requirements meets.
 */
public class LightMergerChunkTaskProvider implements Function<ForkJoinTask<Chunk>, AbstractChunkTask>,
        ChunkTaskListener,
        ChunkRemoveFromPipelineListener {

    private final ChunkProvider chunkProvider;
    private final ChunkProcessingPipeline pipeline;
    private final Map<Vector3i, SettableFuture<Chunk>> requiredChunks = Maps.newConcurrentMap();
    private final Map<Vector3i, Chunk> noticedChunkInProcessing = new ConcurrentHashMap<>();

    public LightMergerChunkTaskProvider(ChunkProvider chunkProvider,
                                        ChunkProcessingPipeline pipeline) {
        this.chunkProvider = chunkProvider;
        this.pipeline = pipeline;
    }

    @Override
    public AbstractChunkTask apply(ForkJoinTask<Chunk> chunkFuture) {
        AbstractChunkTask chunkTask = (AbstractChunkTask) chunkFuture;
        requiredChunks
                .computeIfAbsent(chunkTask.getPosition(), (pos) -> SettableFuture.create()).setFuture(JdkFutureAdapters.listenInPoolThread(chunkFuture));

        Future<Chunk>[] localFutures = getNearPositions(chunkTask.getPosition()).map(
                (p) -> {
                    SettableFuture<Chunk> future = requiredChunks.computeIfAbsent(p, (pos) -> SettableFuture.create());

                    Chunk chunk = chunkProvider.getChunk(JomlUtil.from(p));
                    if (chunk != null) {
                        future.set(chunk);
                    }
                    return future;
                }
        ).toArray(Future[]::new);

        return makeTask(chunkTask, localFutures);
    }

    /**
     * {@inheritDoc}
     *
     * @param chunkTask ChunkTask which done processing.
     */
    @Override
    public void onDone(ChunkTask chunkTask) {

    }

    @NotNull
    private LightMergerChunkTask makeTask(ForkJoinTask<Chunk> chunk, Future<Chunk>[] localChunks) {
        return new LightMergerChunkTask(chunk, localChunks, new LightMerger());
    }

    private Stream<Vector3i> getNearPositions(Vector3i chunkPosition) {
        BlockRegion extentsRegion = new BlockRegion(
                chunkPosition.x - 1, chunkPosition.y - 1, chunkPosition.z - 1,
                chunkPosition.x + 1, chunkPosition.y + 1, chunkPosition.z + 1);
        return StreamSupport.stream(BlockRegionIterable
                        .region(extentsRegion)
                        .build()
                        .spliterator(),
                false)
                .map(Vector3i::new);
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

}

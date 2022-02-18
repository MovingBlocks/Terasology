// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.world;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.joml.Vector3ic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.core.GameScheduler;
import org.terasology.engine.monitoring.chunk.ChunkMonitor;
import org.terasology.engine.rendering.primitives.ChunkTessellator;
import org.terasology.engine.rendering.primitives.MutableChunkMesh;
import org.terasology.engine.rendering.world.viewDistance.ViewDistance;
import org.terasology.engine.world.ChunkView;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.chunks.Chunk;
import org.terasology.engine.world.chunks.RenderableChunk;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Scheduler;
import reactor.function.TupleUtils;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * Receives RenderableChunks, works to make sure their Mesh is up-to-date.
 * <p>
 * Prioritizes work according to the given comparator function.
 * <p>
 * TODO:
 * <ul>
 *  <li> How many of these do we expect to create?
 *  <li> What's its lifetime?
 *  <li> Is there anything we need to do for shutdown / cleanup?
 *  <li> Need it be public, or is it internal to engine.rendering.world?
 * </ul>
 */
public final class ChunkMeshWorker {
    private static final Logger logger = LoggerFactory.getLogger(ChunkMeshWorker.class);

    private static final int MAX_LOADABLE_CHUNKS =
            ViewDistance.MEGA.getChunkDistance().x() * ViewDistance.MEGA.getChunkDistance().y() * ViewDistance.MEGA.getChunkDistance().z();

    private final Comparator<RenderableChunk> frontToBackComparator;
    private final Set<Vector3ic> chunkMeshProcessing = Sets.newConcurrentHashSet();

    private final Sinks.Many<Chunk> chunkMeshPublisher = Sinks.many().unicast().onBackpressureBuffer();
    private final List<Chunk> chunksInProximityOfCamera = Lists.newArrayListWithCapacity(MAX_LOADABLE_CHUNKS);
    private final Flux<Tuple2<Chunk, MutableChunkMesh>> chunksAndNewMeshes;
    private final Flux<Chunk> completedChunks;

    ChunkMeshWorker(Function<? super Chunk, Mono<Tuple2<Chunk, MutableChunkMesh>>> workFunction,
                    Comparator<RenderableChunk> frontToBackComparator, Scheduler parallelScheduler, Scheduler graphicsScheduler) {
        this.frontToBackComparator = frontToBackComparator;

        chunksAndNewMeshes = chunkMeshPublisher.asFlux()
                .distinct(Chunk::getPosition, () -> chunkMeshProcessing)
                .parallel().runOn(parallelScheduler)
                .flatMap(workFunction)
                .sequential();

        completedChunks = chunksAndNewMeshes
                .publishOn(graphicsScheduler)
                .map(TupleUtils.function(ChunkMeshWorker::uploadNewMesh))
                .doOnNext(chunk -> chunkMeshProcessing.remove(chunk.getPosition()));

        // FIXME: error handling???
        //     throwable -> logger.error("Failed to build mesh {}", throwable);
    }

    public static ChunkMeshWorker create(ChunkTessellator chunkTessellator,
                                         WorldProvider worldProvider,
                                         Comparator<RenderableChunk> frontToBackComparator) {
        ChunkMeshWorker worker = new ChunkMeshWorker(generateMeshFunc(chunkTessellator, worldProvider),
                frontToBackComparator,
                GameScheduler.parallel(), GameScheduler.gameMain());
        worker.completedChunks.subscribe();
        return worker;
    }
    
    public void add(Chunk chunk) {
        // TODO: avoid adding duplicates
        chunksInProximityOfCamera.add(chunk);
    }

    public void remove(Chunk chunk) {
        chunkMeshProcessing.remove(chunk.getPosition());

        chunksInProximityOfCamera.remove(chunk);
        chunk.disposeMesh();
    }

    public void remove(Vector3ic coord) {
        chunkMeshProcessing.remove(coord);

        Iterator<Chunk> iterator = chunksInProximityOfCamera.iterator();
        while (iterator.hasNext()) {
            Chunk chunk = iterator.next();
            if (chunk.getPosition().equals(coord)) {
                chunk.disposeMesh();
                iterator.remove();
                break;
            }
        }
    }

    /**
     * Queue all dirty items in our collection, in priority order.
     *
     * @return the number of dirty chunks added to the queue
     */
    public int update() {
        int statDirtyChunks = 0;
        chunksInProximityOfCamera.sort(frontToBackComparator);
        for (Chunk chunk : chunksInProximityOfCamera) {
            if (!chunk.isReady()) {
                // Chunk was added as part of some region, but not yet ready.
                // Leave it here with the expectation that it will be ready later.
                continue;
            }
            if (!chunk.isDirty()) {
                // Chunk is in proximity list, but is no longer dirty. Probably already processed.
                // Will poll it again next tick to see if it got dirty since then.
                continue;
            }
            statDirtyChunks++;
            Sinks.EmitResult result = chunkMeshPublisher.tryEmitNext(chunk);
            if (result.isFailure()) {
                logger.error("failed to process chunk {} : {}", chunk, result);
            }
        }
        return statDirtyChunks;
    }

    public int numberChunkMeshProcessing() {
        return chunkMeshProcessing.size();
    }

    public Collection<Chunk> chunks() {
        return chunksInProximityOfCamera;
    }

    Flux<Chunk> getCompletedChunks() {
        return completedChunks;
    }

    private static Chunk uploadNewMesh(Chunk chunk, MutableChunkMesh chunkMesh) {
        chunkMesh.updateMesh();  // Does GL stuff, must be on main thread!
        chunkMesh.discardData();
        chunk.setMesh(chunkMesh);
        return chunk;
    }

    private static Function<Chunk, Mono<Tuple2<Chunk, MutableChunkMesh>>> generateMeshFunc(
            ChunkTessellator chunkTessellator, WorldProvider worldProvider) {
        return (chunk -> {
            chunk.setDirty(false);
            ChunkView chunkView = worldProvider.getLocalView(chunk.getPosition());
            if (chunkView != null && chunkView.isValidView()) {
                MutableChunkMesh newMesh = chunkTessellator.generateMesh(chunkView);
                ChunkMonitor.fireChunkTessellated(chunk, newMesh);
                return Mono.just(Tuples.of(chunk, newMesh));
            }
            return Mono.empty();
        });
    }
}

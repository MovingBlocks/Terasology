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
import org.terasology.engine.rendering.primitives.ChunkMesh;
import org.terasology.engine.rendering.primitives.ChunkTessellator;
import org.terasology.engine.rendering.world.viewDistance.ViewDistance;
import org.terasology.engine.world.ChunkView;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.chunks.Chunk;
import org.terasology.engine.world.chunks.RenderableChunk;
import reactor.core.publisher.Sinks;
import reactor.function.TupleUtils;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
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

    ChunkMeshWorker(Function<? super Chunk, Optional<Tuple2<Chunk, ChunkMesh>>> workFunction,
                    Comparator<RenderableChunk> frontToBackComparator) {
        this.frontToBackComparator = frontToBackComparator;

        chunkMeshPublisher.asFlux()
                .distinct(Chunk::getPosition, () -> chunkMeshProcessing)
                .doOnNext(k -> k.setDirty(false))
                .parallel(5).runOn(GameScheduler.parallel())
                .<Optional<Tuple2<Chunk, ChunkMesh>>>map(workFunction)
                .filter(Optional::isPresent).sequential()
                .publishOn(GameScheduler.gameMain())
                .subscribe(result -> result.ifPresent(TupleUtils.consumer((chunk, chunkMesh) -> {
                    if (chunksInProximityOfCamera.contains(chunk)) {
                        chunkMesh.updateMesh();  // Does GL stuff, must be on main thread!
                        chunkMesh.discardData();
                        if (chunk.hasMesh()) {
                            chunk.getMesh().dispose();
                        }
                        chunk.setMesh(chunkMesh);
                    }

                })), throwable -> logger.error("Failed to build mesh {}", throwable));
    }

    public static ChunkMeshWorker create(ChunkTessellator chunkTessellator,
                                         WorldProvider worldProvider,
                                         Comparator<RenderableChunk> frontToBackComparator) {
        return new ChunkMeshWorker(new MeshGenerator(chunkTessellator, worldProvider)::generate, frontToBackComparator);
    }
    
    public void add(Chunk chunk) {
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

    public int update() {
        int statDirtyChunks = 0;
        chunksInProximityOfCamera.sort(frontToBackComparator);
        for (Chunk chunk : chunksInProximityOfCamera) {
            if (chunk.isReady() && chunk.isDirty()) {
                statDirtyChunks++;
                Sinks.EmitResult result = chunkMeshPublisher.tryEmitNext(chunk);
                if (result.isFailure()) {
                    logger.error("failed to process chunk {} : {}", chunk, result);
                }
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


    static class MeshGenerator {
        private final ChunkTessellator chunkTessellator;
        private final WorldProvider worldProvider;

        MeshGenerator(ChunkTessellator chunkTessellator, WorldProvider worldProvider) {
            this.chunkTessellator = chunkTessellator;
            this.worldProvider = worldProvider;
        }

        Optional<Tuple2<Chunk, ChunkMesh>> generate(Chunk chunk) {
            ChunkView chunkView = worldProvider.getLocalView(chunk.getPosition());
            if (chunkView != null && chunkView.isValidView() /* && chunkMeshProcessing.remove(chunk.getPosition()) */) {
                ChunkMesh newMesh = chunkTessellator.generateMesh(chunkView);
                ChunkMonitor.fireChunkTessellated(chunk, newMesh);
                return Optional.of(Tuples.of(chunk, newMesh));
            }
            return Optional.empty();
        }
    }
}

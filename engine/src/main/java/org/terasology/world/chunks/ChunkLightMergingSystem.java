// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.world.chunks;

import com.google.common.collect.Sets;
import org.joml.Vector3ic;
import org.terasology.engine.subsystem.common.ThreadManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.registry.In;
import org.terasology.world.block.BlockRegion;
import org.terasology.world.chunks.event.BeforeChunkUnload;
import org.terasology.world.chunks.event.OnChunkLoaded;
import org.terasology.world.propagation.light.LightMerger;

import java.util.Set;

/**
 * System which will process light merging after chunk is ready.
 */
@RegisterSystem
public class ChunkLightMergingSystem extends BaseComponentSystem {

    private final Set<Vector3ic> loadedChunkPos = Sets.newConcurrentHashSet();
    private final Set<Vector3ic> lightMergedChunks = Sets.newConcurrentHashSet();

    @In
    ChunkProvider chunkProvider;
    @In
    ThreadManager threadManager;

    @ReceiveEvent
    public void onChunkLoad(OnChunkLoaded chunkLoaded, EntityRef world) {
        loadedChunkPos.add(chunkLoaded.getChunkPos());
        processNearbyChunks(chunkLoaded.getChunkPos());
    }

    private void processNearbyChunks(Vector3ic chunkPos) {
        for (Vector3ic candidateForLightMerging : new BlockRegion(chunkPos).expand(1, 1, 1)) {
            if (lightMergedChunks.contains(candidateForLightMerging)) {
                continue;
            }
            processCandidateChunk(candidateForLightMerging);
            lightMergedChunks.add(candidateForLightMerging);
        }
    }

    /**
     * Check nearby chunks, gather nearby chunks and starts light merging, if nearby chunks is ready.
     *
     * @param candidateChunkPosition candidate for lightmerging.
     */
    private void processCandidateChunk(Vector3ic candidateChunkPosition) {
        BlockRegion around = new BlockRegion(candidateChunkPosition).expand(1, 1, 1);
        Chunk[] chunks = new Chunk[LightMerger.LOCAL_CHUNKS_ARRAY_LENGTH];
        int index = 0;
        for (Vector3ic position : around) {
            if (!loadedChunkPos.contains(position)) {
                return;
            }
            chunks[index++] = chunkProvider.getChunk(position);
        }
        threadManager.submitTask("Chunk Light Merging", () -> new LightMerger().merge(chunks));
    }

    @ReceiveEvent
    public void onChunkUnload(BeforeChunkUnload chunkUnload, EntityRef world) {
        loadedChunkPos.remove(chunkUnload.getChunkPos());
        lightMergedChunks.remove(chunkUnload.getChunkPos());
    }

}

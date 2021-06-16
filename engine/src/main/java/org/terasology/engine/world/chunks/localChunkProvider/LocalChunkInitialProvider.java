// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.chunks.localChunkProvider;

import com.google.common.collect.Streams;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.slf4j.LoggerFactory;
import org.terasology.engine.persistence.ChunkStore;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.engine.world.chunks.Chunk;
import org.terasology.engine.world.chunks.internal.ChunkImpl;
import org.terasology.engine.world.chunks.internal.ChunkRelevanceRegion;
import org.terasology.engine.world.chunks.pipeline.InitialChunkProvider;
import org.terasology.engine.world.generation.impl.EntityBufferImpl;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class LocalChunkInitialProvider implements InitialChunkProvider {
    private LocalChunkProvider chunkProvider;
    private RelevanceSystem relevanceSystem;

    private List<Vector3ic> chunksInRange;
    private BlockRegion[] lastRegions;

    public LocalChunkInitialProvider(LocalChunkProvider chunkProvider, RelevanceSystem relevanceSystem) {
        this.chunkProvider = chunkProvider;
        this.relevanceSystem = relevanceSystem;
        chunksInRange = new ArrayList<>();
        updateList();
    }

    private void updateList() {
        relevanceSystem.neededChunks().forEach(chunkPos -> {
            if (!chunksInRange.contains(chunkPos)) {
                chunksInRange.add(chunkPos);
            }
        });
        chunksInRange.removeIf(x -> !relevanceSystem.isChunkInRegions(x) || chunkProvider.isChunkReady(x));
        chunksInRange.sort(relevanceSystem.createChunkPosComparator().reversed());
    }

    private boolean checkForUpdate() {
        Collection<ChunkRelevanceRegion> regions = relevanceSystem.getRegions();
        if (lastRegions == null || regions.size() != lastRegions.length) {
            lastRegions = regions.stream().map(ChunkRelevanceRegion::getCurrentRegion).toArray(BlockRegion[]::new);
            return true;
        }
        int i = 0;
        boolean anyChanged = false;
        for (ChunkRelevanceRegion region : regions) {
            if (!lastRegions[i].equals(region.getCurrentRegion())) {
                lastRegions[i].set(region.getCurrentRegion());
                anyChanged = true;
            }
            i++;
        }
        return anyChanged;
    }

    @Override
    public boolean hasNext() {
        if (checkForUpdate()) {
            updateList();
        }
        return !chunksInRange.isEmpty();
    }

    @Override
    public Chunk next(Set<Vector3ic> currentlyGenerating) {
        while (hasNext()) {
            Vector3ic pos = chunksInRange.remove(chunksInRange.size() - 1);
            if (currentlyGenerating.contains(pos)) {
                continue;
            }

            return chunkProvider.getInitialChunk(pos);
        }
        return null;
    }
}

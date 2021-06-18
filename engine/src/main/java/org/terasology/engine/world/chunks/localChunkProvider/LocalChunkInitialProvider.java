// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.chunks.localChunkProvider;

import org.joml.Vector3ic;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.engine.world.chunks.Chunk;
import org.terasology.engine.world.chunks.internal.ChunkRelevanceRegion;
import org.terasology.engine.world.chunks.pipeline.InitialChunkProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class LocalChunkInitialProvider implements InitialChunkProvider {
    private final LocalChunkProvider chunkProvider;
    private final RelevanceSystem relevanceSystem;

    private final List<Vector3ic> chunksInRange;
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
    public Optional<Chunk> next(Set<Vector3ic> currentlyGenerating) {
        while (true) {
            Vector3ic pos;
            synchronized (this) {
                if (checkForUpdate()) {
                    updateList();
                }
                if (chunksInRange.isEmpty()) {
                    break;
                }
                pos = chunksInRange.remove(chunksInRange.size() - 1);
            }
            if (currentlyGenerating.contains(pos)) {
                continue;
            }

            return Optional.of(chunkProvider.getInitialChunk(pos));
        }
        return Optional.empty();
    }
}

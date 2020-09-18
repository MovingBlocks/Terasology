// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.world.chunks.pipeline.stages;

import org.terasology.world.chunks.Chunk;
import org.terasology.world.propagation.light.LightMerger;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class LightMergerProcessingStageProvider implements StageProvider {

    @Override
    public FunctionalStage apply(Collection<CompletableFuture<Chunk>> nearChunks) {

        return new FunctionalStage((e, future) -> future.thenApplyAsync(chunk -> {
            CompletableFuture<Void> allFutures =
                    CompletableFuture.allOf(nearChunks.toArray(new CompletableFuture[0]));
            Chunk[] localChunks =
                    allFutures.thenApply(v -> nearChunks.stream().map(CompletableFuture::join)).join().toArray(Chunk[]::new);
            new LightMerger().merge(chunk, localChunks);
            return chunk;
        }, e));
    }
}

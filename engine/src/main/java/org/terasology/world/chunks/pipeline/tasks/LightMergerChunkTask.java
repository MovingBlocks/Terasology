// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.world.chunks.pipeline.tasks;

import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.pipeline.AbstractChunkTask;
import org.terasology.world.propagation.light.LightMerger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.Future;

/**
 * Chunk task for run {@link LightMerger#merge(Chunk, Chunk[])};
 */
public class LightMergerChunkTask extends AbstractChunkTask {

    private final Future<Chunk>[] localChunks;
    private final LightMerger lightMerger;

    public LightMergerChunkTask(ForkJoinTask<Chunk> chunk, Future<Chunk>[] localChunks, LightMerger lightMerger) {
        super(chunk);
        this.localChunks = localChunks;
        this.lightMerger = lightMerger;
    }

    @Override
    public String getName() {
        return "Light Merging";
    }

    @Override
    public void run() {

    }

    @Override
    protected boolean exec() {
        setRawResult(chunkFuture.fork().join());

        List<Chunk> list = new ArrayList<>();
        for (Future<Chunk> localChunk : localChunks) {
            try {
                list.add(localChunk.get());
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                return false;
            }
        }
        Chunk[] chunks = list.toArray(new Chunk[0]);
        lightMerger.merge(getRawResult(), chunks);
        return true;
    }
}

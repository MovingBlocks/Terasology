// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.world.chunks.pipeline.tasks;

import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.pipeline.AbstractChunkTask;

import java.util.concurrent.ForkJoinTask;

/**
 * ChunkTask for run {@link Chunk#deflate()}
 */
public class DeflateChunkTask extends AbstractChunkTask {
    public DeflateChunkTask(ForkJoinTask<Chunk> chunk) {
        super(chunk);
    }

    @Override
    public String getName() {
        return "Chunk deflate";
    }

    @Override
    public void run() {

    }

    @Override
    protected boolean exec() {
        setRawResult(chunkFuture.fork().join());
        getRawResult().deflate();
        return true;
    }
}

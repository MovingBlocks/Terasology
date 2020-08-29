// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.world.chunks.pipeline.tasks;

import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.pipeline.AbstractChunkTask;

public class DeflateChunkTask extends AbstractChunkTask {

    public DeflateChunkTask(Chunk chunk) {
        super(chunk);
    }

    @Override
    public String getName() {
        return "Chunk deflate";
    }

    @Override
    public void run() {
        chunk.deflate();
    }


}

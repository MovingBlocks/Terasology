/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.world.chunks.localChunkProvider;

import org.terasology.utilities.concurrency.Task;
import org.terasology.world.chunks.Chunk;

/**
 */
public class ChunkUnloadRequest implements Task {

    private Chunk chunk;
    private LocalChunkProvider chunkProvider;
    private boolean shutdown;

    public ChunkUnloadRequest(Chunk chunk, LocalChunkProvider localChunkProvider) {
        this.chunk = chunk;
        this.chunkProvider = localChunkProvider;
    }

    public ChunkUnloadRequest() {
        shutdown = true;
    }

    @Override
    public String getName() {
        return "Unload chunk";
    }

    @Override
    public void run() {
        if (!shutdown) {
            chunkProvider.gatherBlockPositionsForDeactivate(chunk);
        }
    }

    @Override
    public boolean isTerminateSignal() {
        return shutdown;
    }
}

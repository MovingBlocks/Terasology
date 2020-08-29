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

package org.terasology.world.chunks.pipeline;

import org.joml.Vector3i;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.Chunk;

/**
 */
public abstract class AbstractChunkTask implements ChunkTask {
    protected Chunk chunk;

    public AbstractChunkTask() {
    }

    public AbstractChunkTask(Chunk chunk) {
        this.chunk = chunk;
    }

    @Override
    public Chunk getChunk() {
        return chunk;
    }

    @Override
    public boolean isTerminateSignal() {
        return false;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ChunkTask{");
        sb.append("name = ").append(getName()).append(",");
        sb.append("position = ").append(getPosition());
        sb.append('}');
        return sb.toString();
    }
}

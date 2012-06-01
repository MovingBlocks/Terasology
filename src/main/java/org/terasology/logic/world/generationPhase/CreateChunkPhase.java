/*
 * Copyright 2012
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

package org.terasology.logic.world.generationPhase;

import org.terasology.logic.world.Chunk;
import org.terasology.logic.world.generator.core.ChunkGeneratorManager;
import org.terasology.math.Vector3i;

import java.util.Comparator;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Immortius
 */
public class CreateChunkPhase extends ChunkPhase {

    private ConcurrentMap<Vector3i, Chunk> nearCache;
    private ChunkGeneratorManager generator;

    public CreateChunkPhase(int numThreads, Comparator<Vector3i> chunkRelevanceComparator, ChunkGeneratorManager generator, ConcurrentMap<Vector3i, Chunk> nearCache) {
        super(numThreads, chunkRelevanceComparator);
        this.generator = generator;
        this.nearCache = nearCache;
    }

    @Override
    protected void process(Vector3i pos) {
        Chunk chunk = generator.generateChunk(pos);
        nearCache.putIfAbsent(pos, chunk);
    }
}

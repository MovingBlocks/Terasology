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

import org.terasology.logic.world.*;
import org.terasology.math.Vector3i;

import java.util.Comparator;

/**
 * @author Immortius
 */
public class PropagateLightingPhase extends ChunkPhase {

    private ChunkProvider chunkProvider;
    private ChunkGeneratorManager generator;

    public PropagateLightingPhase(int numThreads, Comparator<Vector3i> chunkRelevanceComparator, ChunkGeneratorManager generator, ChunkProvider chunkProvider) {
        super(numThreads, chunkRelevanceComparator);
        this.generator = generator;
        this.chunkProvider = chunkProvider;
    }

    @Override
    protected void process(Vector3i pos) {
        WorldView worldView = WorldView.createLocalView(pos, chunkProvider);
        worldView.lock();
        try {
            new LightPropagator(worldView).propagateOutOfTargetChunk();
            chunkProvider.getChunk(pos).setChunkState(Chunk.State.FullLightConnectivityPending);
        } finally {
            worldView.unlock();
        }
    }
}

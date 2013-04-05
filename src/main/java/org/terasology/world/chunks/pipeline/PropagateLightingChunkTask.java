/*
 * Copyright 2013 Moving Blocks
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

import org.terasology.math.Region3i;
import org.terasology.math.Vector3i;
import org.terasology.world.ChunkView;
import org.terasology.world.RegionalChunkView;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.ChunkConstants;
import org.terasology.world.chunks.ChunkProvider;
import org.terasology.world.chunks.internal.GeneratingChunkProvider;
import org.terasology.world.lighting.LightPropagator;

/**
 * @author Immortius
 */
public class PropagateLightingChunkTask extends AbstractChunkTask {

    public PropagateLightingChunkTask(ChunkGenerationPipeline pipeline, Vector3i position, GeneratingChunkProvider provider) {
        super(pipeline, position, provider);
    }

    @Override
    public void enact() {
        ChunkView chunkView = getProvider().getViewAround(getPosition());
        if (chunkView == null) {
            return;
        }
        chunkView.lock();
        try {
            if (!chunkView.isValidView()) {
                return;
            }
            Chunk chunk = getProvider().getChunkForProcessing(getPosition());
            if (chunk.getChunkState() != Chunk.State.LIGHT_PROPAGATION_PENDING) {
                return;
            }

            new LightPropagator(chunkView).propagateOutOfTargetChunk();
            chunk.setChunkState(Chunk.State.FULL_LIGHT_CONNECTIVITY_PENDING);
            getPipeline().requestReview(Region3i.createFromCenterExtents(getPosition(), ChunkConstants.LOCAL_REGION_EXTENTS));
        } finally {
            chunkView.unlock();
        }
    }
}

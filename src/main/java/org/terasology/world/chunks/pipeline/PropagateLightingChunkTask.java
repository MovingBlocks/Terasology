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

import com.google.common.collect.Lists;
import org.terasology.math.Region3i;
import org.terasology.math.Vector3i;
import org.terasology.world.ChunkView;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.ChunkConstants;
import org.terasology.world.chunks.internal.GeneratingChunkProvider;
import org.terasology.world.propagation.BatchPropagator;
import org.terasology.world.propagation.light.LightChunkView;
import org.terasology.world.propagation.light.LightPropagationRules;
import org.terasology.world.propagation.light.SunlightChunkView;
import org.terasology.world.propagation.light.SunlightPropagationRules;

import java.util.List;

/**
 * @author Immortius
 */
public class PropagateLightingChunkTask extends AbstractChunkTask {

    public PropagateLightingChunkTask(ChunkGenerationPipeline pipeline, Vector3i position, GeneratingChunkProvider provider) {
        super(pipeline, position, provider);
    }

    @Override
    public String getName() {
        return "Chunk lighting";
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

            Vector3i chunkMin = new Vector3i(chunk.getPos().x * Chunk.SIZE_X, chunk.getPos().y * Chunk.SIZE_Y, chunk.getPos().z * Chunk.SIZE_Z);
            List<Region3i> externalSurfaces = Lists.newArrayList();
            externalSurfaces.add(Region3i.createFromMinAndSize(chunkMin, new Vector3i(Chunk.SIZE_X - 1, Chunk.SIZE_Y, 1)));
            externalSurfaces.add(Region3i.createFromMinAndSize(new Vector3i(chunkMin.x + Chunk.SIZE_X - 1, chunkMin.y, chunkMin.z),
                    new Vector3i(1, Chunk.SIZE_Y, Chunk.SIZE_Z - 1)));
            externalSurfaces.add(Region3i.createFromMinAndSize(new Vector3i(chunkMin.x, chunkMin.y, chunkMin.z + 1), new Vector3i(1, Chunk.SIZE_Y, Chunk.SIZE_Z - 1)));
            externalSurfaces.add(Region3i.createFromMinAndSize(new Vector3i(chunkMin.x + 1, chunkMin.y, chunkMin.z + Chunk.SIZE_Z - 1),
                    new Vector3i(Chunk.SIZE_X - 1, Chunk.SIZE_Y, 1)));

            new BatchPropagator(new SunlightPropagationRules(), new SunlightChunkView(chunkView)).propagateFrom(externalSurfaces);
            new BatchPropagator(new LightPropagationRules(), new LightChunkView(chunkView)).propagateFrom(externalSurfaces);

            chunk.setChunkState(Chunk.State.FULL_LIGHT_CONNECTIVITY_PENDING);
            getPipeline().requestReview(Region3i.createFromCenterExtents(getPosition(), ChunkConstants.LOCAL_REGION_EXTENTS));
        } finally {
            chunkView.unlock();
        }
    }
}

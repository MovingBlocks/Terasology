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

import org.terasology.math.Region3i;
import org.terasology.math.Vector3i;
import org.terasology.world.ChunkView;
import org.terasology.world.chunks.ChunkConstants;
import org.terasology.world.chunks.internal.ChunkImpl;
import org.terasology.world.chunks.internal.GeneratingChunkProvider;

/**
 * @author Immortius
 */
public class SecondPassChunkTask extends AbstractChunkTask {

    public SecondPassChunkTask(ChunkGenerationPipeline pipeline, Vector3i position, GeneratingChunkProvider provider) {
        super(pipeline, position, provider);
    }

    @Override
    public String getName() {
        return "Chunk second pass";
    }

    @Override
    public void run() {
        ChunkView view = getProvider().getViewAround(getPosition());
        if (view == null) {
            return;
        }
        view.lock();
        try {
            if (!view.isValidView()) {
                return;
            }
            ChunkImpl chunk = getProvider().getChunkForProcessing(getPosition());
            if (chunk.getChunkState() != ChunkImpl.State.ADJACENCY_GENERATION_PENDING) {
                return;
            }

            getPipeline().getWorldGenerator().applySecondPass(getPosition(), view);
            chunk.setChunkState(ChunkImpl.State.INTERNAL_LIGHT_GENERATION_PENDING);
            getPipeline().requestReview(Region3i.createFromCenterExtents(getPosition(), ChunkConstants.SECOND_PASS_EXTENTS));
        } finally {
            view.unlock();
        }
    }

}

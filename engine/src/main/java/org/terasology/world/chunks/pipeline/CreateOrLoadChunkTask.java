/*
 * Copyright 2014 MovingBlocks
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

import org.terasology.math.Vector3i;
import org.terasology.world.chunks.internal.GeneratingChunkProvider;

/**
 * @author Immortius
 */
public class CreateOrLoadChunkTask extends AbstractChunkTask {

    public CreateOrLoadChunkTask(ChunkGenerationPipeline pipeline, Vector3i position, GeneratingChunkProvider provider) {
        super(position, provider);
    }

    @Override
    public String getName() {
        return "Create or Load Chunk";
    }

    @Override
    public void run() {
        getProvider().createOrLoadChunk(getPosition());
    }
}

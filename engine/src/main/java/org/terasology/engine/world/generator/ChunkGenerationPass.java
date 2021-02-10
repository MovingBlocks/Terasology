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

package org.terasology.world.generator;

import org.terasology.world.chunks.CoreChunk;

import java.util.Map;

/**
 */
public interface ChunkGenerationPass {

    void setWorldSeed(String seed);

    Map<String, String> getInitParameters();

    void setInitParameters(Map<String, String> initParameters);

    /**
     * Generate the local contents of a chunk. This should be purely deterministic from the chunk contents, chunk
     * position and world seed - should not depend on external state or other data.
     *
     * @param chunk
     */
    void generateChunk(CoreChunk chunk);

}

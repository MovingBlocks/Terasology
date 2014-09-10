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

import org.terasology.engine.SimpleUri;
import org.terasology.math.Vector3i;
import org.terasology.world.ChunkView;
import org.terasology.world.chunks.Chunk;

import com.google.common.base.Optional;

/**
 * @author Immortius
 */
public interface WorldGenerator {
    SimpleUri getUri();

    void setWorldSeed(String seed);

    void applySecondPass(Vector3i chunkPos, ChunkView view);

    void createChunk(Chunk chunk);

    float getFog(float x, float y, float z);

    float getTemperature(float x, float y, float z);

    float getHumidity(float x, float y, float z);

    void initialize();

    Optional<WorldConfigurator> getConfigurator();
}

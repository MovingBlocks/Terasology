/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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

package org.terasology.logic.world.generator.core;

import java.util.List;

import org.terasology.logic.world.WorldBiomeProvider;
import org.terasology.logic.world.WorldView;
import org.terasology.logic.world.chunks.Chunk;
import org.terasology.logic.world.generator.BaseChunkGenerator;
import org.terasology.math.Vector3i;

/**
 * @author Immortius
 */
public interface ChunkGeneratorManager {

    List<BaseChunkGenerator> getBaseChunkGenerators();

    void setWorldSeed(String seed);

    void setWorldBiomeProvider(WorldBiomeProvider biomeProvider);

    void registerChunkGenerator(BaseChunkGenerator generator);

    Chunk generateChunk(Vector3i pos);

    void secondPassChunk(Vector3i chunkPos, WorldView view);

}

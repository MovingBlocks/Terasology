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

package org.terasology.logic.newWorld;

import org.terasology.math.Vector3i;

/**
 * @author Immortius
 */
public class NullChunkGeneratorManager implements NewChunkGeneratorManager{
    @Override
    public void setWorldSeed(String seed) {
    }

    @Override
    public void setWorldBiomeProvider(WorldBiomeProvider biomeProvider) {
    }

    @Override
    public void registerChunkGenerator(NewChunkGenerator generator) {
    }

    @Override
    public NewChunk generateChunk(Vector3i pos) {
        return new NewChunk(pos.x, pos.y, pos.z);
    }
}

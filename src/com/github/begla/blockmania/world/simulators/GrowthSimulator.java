/*
 * Copyright 2011 Benjamin Glatzel <benjamin.glatzel@me.com>.
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
package com.github.begla.blockmania.world.simulators;

import com.github.begla.blockmania.world.chunk.Chunk;
import com.github.begla.blockmania.world.main.WorldProvider;

/**
 * TODO
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class GrowthSimulator extends Simulator {

    public GrowthSimulator(WorldProvider parent) {
        super(parent);
    }

    @Override
    public void simulate() {
        // Apply simulator to all active chunks
        for (int i = 0; i < _activeChunks.size(); i++) {
            growGrass(_activeChunks.get(i));
        }
    }

    private void growGrass(Chunk c) {
        for (int x = 0; x < Chunk.getChunkDimensionX(); x++) {
            for (int z = 0; z < Chunk.getChunkDimensionZ(); z++) {

                for (int y = Chunk.getChunkDimensionY() - 1; y >= 0; y--) {

                }

            }
        }
    }

}


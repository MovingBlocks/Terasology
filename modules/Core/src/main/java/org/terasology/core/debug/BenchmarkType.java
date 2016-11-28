/*
 * Copyright 2016 MovingBlocks
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
package org.terasology.core.debug;

import org.terasology.context.Context;
import org.terasology.world.chunks.ChunkConstants;

/**
 * Benchmark types the user can select and start.
 */
enum BenchmarkType {
    WORLD_PROVIDER_SET_BLOCK("WorldProvider.setBlock", BenchmarkScreen.DEFAULT_ITERATION_COUNT) {
        @Override
        public AbstractBenchmarkInstance createInstance(Context context) {
            return new BlockPlacementBenchmark(context, false);
        }

        @Override
        public String getDescription() {
            return "Uses setBlock of WorldProvder to replace the chunk (" + BLOCKS_PER_CHUNK + " blocks) above" +
                    " the player one iteration with stone the other iteration with air";
        }
    },

    WORLD_PROVIDER_SET_BLOCKs("WorldProvider.setBlocks", BenchmarkScreen.DEFAULT_ITERATION_COUNT) {
        @Override
        public AbstractBenchmarkInstance createInstance(Context context) {
            return new BlockPlacementBenchmark(context, true);
        }

        @Override
        public String getDescription() {
            return "Uses setBlocks of WorldProvder to replace the chunk (" + BLOCKS_PER_CHUNK + " blocks) above" +
                    " the player one iteration with stone the other iteration with air";
        }
    };

    private static final int BLOCKS_PER_CHUNK = ChunkConstants.CHUNK_SIZE.x * ChunkConstants.CHUNK_SIZE.y
            * ChunkConstants.CHUNK_SIZE.z;

    private String title;
    private int maxIterations;

    BenchmarkType(String title, int maxIterations) {
        this.title = title;
        this.maxIterations = maxIterations;
    }

    @Override
    public String toString() {
        return title;
    }

    /**
     * @return a runnable that will be invoked each iteration, and duration of the invokation will be recorded.
     */
    public abstract AbstractBenchmarkInstance createInstance(Context context);

    /**
     *
     * @return a description of the benchmark type.
     */
    public abstract String getDescription();

    public String getTitle() {
        return title;
    }

    public int getMaxIterations() {
        return maxIterations;
    }
}

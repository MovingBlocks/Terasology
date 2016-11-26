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

    private BenchmarkType(String title, int maxIterations) {
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

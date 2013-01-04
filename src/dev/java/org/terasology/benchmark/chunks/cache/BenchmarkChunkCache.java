package org.terasology.benchmark.chunks.cache;

import org.terasology.benchmark.Benchmark;
import org.terasology.benchmark.BenchmarkResult;

public class BenchmarkChunkCache extends Benchmark {

    public BenchmarkChunkCache() {
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public int getWarmupRepetitions() {
        return 1;
    }
    
    @Override
    public int[] getRepetitions() {
        return new int[] {1, 10};
    }

    @Override
    public BenchmarkResult createResult() {
        return null;
    }

    @Override
    public void setup() {

    }

    @Override
    public void prerun(int index) {

    }

    @Override
    public int run(int index, int repetitions, BenchmarkResult result) {
        return 0;
    }

    @Override
    public void postrun(int index, BenchmarkResult result) {

    }

    @Override
    public void finish(boolean aborted) {

    }

}

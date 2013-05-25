package org.terasology.benchmark.chunks.cache;

import org.terasology.benchmark.Benchmark;

public class BenchmarkChunkCache implements Benchmark {

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
    public void setup() {

    }

    @Override
    public void prerun() {

    }

    @Override
    public void run() {
    }

    @Override
    public void postrun() {

    }

    @Override
    public void finish(boolean aborted) {

    }

}

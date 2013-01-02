package org.terasology.benchmark.chunks.arrays;

import org.terasology.benchmark.BasicBenchmarkResult;
import org.terasology.benchmark.Benchmark;
import org.terasology.benchmark.BenchmarkResult;
import org.terasology.world.chunks.blockdata.TeraArray;

import com.google.common.base.Preconditions;

/**
 * BenchmarkTeraArray is the base class for benchmarking tera arrays.
 * 
 * @author Manuel Brotz <manu.brotz@gmx.ch>
 *
 */
public abstract class BenchmarkTeraArray extends Benchmark {

    protected TeraArray array;
    
    public BenchmarkTeraArray(TeraArray array) {
        this.array = Preconditions.checkNotNull(array);
    }

    @Override
    public int[] getRepetitions() {
        return new int[] {500, 5000, 50000, 100000};
    }

    @Override
    public BenchmarkResult createResult() {
        return new BasicBenchmarkResult(this);
    }

    @Override
    public void setup() {}

    @Override
    public void prerun(int index) {}

    @Override
    public void postrun(int index, BenchmarkResult result) {}

    @Override
    public void finish(boolean aborted) {}

}

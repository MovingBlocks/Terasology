// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.benchmark.chunks.arrays;

import com.google.common.base.Preconditions;
import org.terasology.benchmark.Benchmark;
import org.terasology.engine.world.chunks.blockdata.TeraArray;

/**
 * BenchmarkTeraArray is the base class for benchmarking tera arrays.
 */
public abstract class BenchmarkTeraArray implements Benchmark {

    protected TeraArray array;

    public BenchmarkTeraArray(TeraArray array) {
        this.array = Preconditions.checkNotNull(array);
    }

    @Override
    public int getWarmupRepetitions() {
        return 10000;
    }

    @Override
    public int[] getRepetitions() {
        return new int[]{500, 5000, 50000, 100000};
    }

    @Override
    public void setup() {
    }

    @Override
    public void prerun() {
    }

    @Override
    public void postrun() {
    }

    @Override
    public void finish(boolean aborted) {
    }

}

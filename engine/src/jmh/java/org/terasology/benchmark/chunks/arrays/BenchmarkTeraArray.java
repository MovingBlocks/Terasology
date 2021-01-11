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
package org.terasology.benchmark.chunks.arrays;

import com.google.common.base.Preconditions;
import org.terasology.benchmark.Benchmark;
import org.terasology.world.chunks.blockdata.TeraArray;

/**
 * BenchmarkTeraArray is the base class for benchmarking tera arrays.
 *
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

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

package org.terasology.benchmark;

import java.util.Arrays;

/**
 *
 */
public abstract class AbstractBenchmark implements Benchmark {

    private String title;
    private int warmupRepetitions;
    private int[] reps;

    public AbstractBenchmark(String title, int warmupReps, int[] reps) {
        this.title = title;
        this.warmupRepetitions = warmupReps;
        this.reps = Arrays.copyOf(reps, reps.length);
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public int getWarmupRepetitions() {
        return warmupRepetitions;
    }

    @Override
    public int[] getRepetitions() {
        return Arrays.copyOf(reps, reps.length);
    }

    @Override
    public void setup() {
    }

    @Override
    public void prerun() {
    }

    @Override
    public abstract void run();

    @Override
    public void postrun() {
    }

    @Override
    public void finish(boolean aborted) {
    }
}

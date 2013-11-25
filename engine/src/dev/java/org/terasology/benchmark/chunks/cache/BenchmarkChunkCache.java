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
        return new int[]{1, 10};
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

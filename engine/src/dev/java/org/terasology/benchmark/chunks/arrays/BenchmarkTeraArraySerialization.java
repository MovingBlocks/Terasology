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

@SuppressWarnings("rawtypes")
public abstract class BenchmarkTeraArraySerialization implements Benchmark {

    public static final int BUFFER_SIZE = 1024 * 1024;

    public final TeraArray.SerializationHandler handler;
    public final TeraArray array;

    @SuppressWarnings("unchecked")
    public BenchmarkTeraArraySerialization(TeraArray.SerializationHandler handler, TeraArray array) {
        this.handler = Preconditions.checkNotNull(handler);
        this.array = Preconditions.checkNotNull(array);
        Preconditions.checkArgument(handler.canHandle(array.getClass()), "The supplied serialization handler is incompatible to the supplied array");
    }

    @Override
    public int getWarmupRepetitions() {
        return 10000;
    }

    @Override
    public int[] getRepetitions() {
        return new int[]{1000, 5000, 10000};
    }

}

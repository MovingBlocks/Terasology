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

package org.terasology.benchmark.reflectFactory;

import com.google.common.collect.Lists;
import org.terasology.benchmark.Benchmarks;
import org.terasology.benchmark.PrintToConsoleCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.benchmark.Benchmark;
import org.terasology.reflection.reflect.ByteCodeReflectFactory;
import org.terasology.reflection.reflect.ReflectionReflectFactory;

import java.util.List;

/**
 *
 */
public final class ReflectFactoryBenchmark {
    private static final Logger logger = LoggerFactory.getLogger(ReflectFactoryBenchmark.class);

    private ReflectFactoryBenchmark() {
    }

    public static void main(String[] args) {
        final List<Benchmark> benchmarks = Lists.newArrayList();

        benchmarks.add(new FieldAccessBenchmark(new ReflectionReflectFactory()));
        benchmarks.add(new FieldAccessBenchmark(new ByteCodeReflectFactory()));
        benchmarks.add(new GetterSetterAccessBenchmark(new ReflectionReflectFactory()));
        benchmarks.add(new GetterSetterAccessBenchmark(new ByteCodeReflectFactory()));
        benchmarks.add(new ConstructionBenchmark(new ReflectionReflectFactory()));
        benchmarks.add(new ConstructionBenchmark(new ByteCodeReflectFactory()));

        Benchmarks.execute(benchmarks, new PrintToConsoleCallback());

    }
}

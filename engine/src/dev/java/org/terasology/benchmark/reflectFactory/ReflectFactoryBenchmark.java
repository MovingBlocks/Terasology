// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.benchmark.reflectFactory;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.benchmark.Benchmark;
import org.terasology.benchmark.Benchmarks;
import org.terasology.benchmark.PrintToConsoleCallback;
import org.terasology.engine.reflection.reflect.ByteCodeReflectFactory;
import org.terasology.nui.reflection.reflect.ReflectionReflectFactory;

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

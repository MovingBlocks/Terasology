// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.benchmark.entitySystem;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.benchmark.Benchmark;
import org.terasology.benchmark.Benchmarks;
import org.terasology.benchmark.PrintToConsoleCallback;

import java.util.List;

/**
 *
 */
public final class EntitySystemBenchmark {
    private static final Logger logger = LoggerFactory.getLogger(EntitySystemBenchmark.class);

    private EntitySystemBenchmark() {
    }

    public static void main(String[] args) {
        final List<Benchmark> benchmarks = Lists.newArrayList();

        benchmarks.add(new EntityCreateBenchmark());
        benchmarks.add(new IterateSingleComponentBenchmark());
        benchmarks.add(new IterateMultipleComponentBenchmark());
        Benchmarks.execute(benchmarks, new PrintToConsoleCallback());

    }
}

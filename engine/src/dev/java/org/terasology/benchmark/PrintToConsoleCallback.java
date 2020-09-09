// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.benchmark;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * PrintToConsoleCallback implements BenchmarkCallback and simply prints everything to the console.
 */
public class PrintToConsoleCallback implements BenchmarkCallback {

    private static final NumberFormat PERCENT_FORMAT = new DecimalFormat("##0.0");

    @Override
    public void begin(Benchmark benchmark, int benchmarkIndex, int benchmarkCount) {
        System.out.println("Benchmark " + benchmarkIndex + " / " + benchmarkCount + ": " + benchmark.getTitle());
    }

    @Override
    public void warmup(Benchmark benchmark, boolean finished) {
        if (finished) {
            System.out.print("Go! ");
        } else {
            System.out.print("Warmup... ");
        }
    }

    @Override
    public void progress(Benchmark benchmark, double percent) {
        System.out.print(PERCENT_FORMAT.format(percent) + "% ");
    }

    @Override
    public void success(BenchmarkResult result) {
        System.out.println();
        System.out.println();
        System.out.println(Benchmarks.printResult(result));
        System.out.println();
    }

    @Override
    public void aborted(BenchmarkResult result) {
        System.out.println();
        System.out.println("Benchmark aborted: " + result.getTitle());
        System.out.println("Number of errors: " + result.getNumErrors());
    }

    @Override
    public void error(BenchmarkError.Type type, Exception e, BenchmarkResult result) {
        System.out.println("Benchmark error of type: " + type);
        e.printStackTrace();
    }

    @Override
    public void fatal(Exception e) {
        System.out.println("Fatal benchmark error: " + e.getClass().getSimpleName());
        e.printStackTrace();
    }

}

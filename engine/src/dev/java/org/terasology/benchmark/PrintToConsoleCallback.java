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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.benchmark.entitySystem.EntitySystemBenchmark;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * PrintToConsoleCallback implements BenchmarkCallback and simply prints everything to the console.
 *
 */
public class PrintToConsoleCallback implements BenchmarkCallback {

    private static final NumberFormat PERCENT_FORMAT = new DecimalFormat("##0.0");

    //private static final Logger LOGGER = Logger.getLogger(PrintToConsoleCallback.class.getName());
    private static final Logger logger = LoggerFactory.getLogger(EntitySystemBenchmark.class);
    @Override
    public void begin(Benchmark benchmark, int benchmarkIndex, int benchmarkCount) {
        //System.out.println("Benchmark " + benchmarkIndex + " / " + benchmarkCount + ": " + benchmark.getTitle());
        logger.info("Benchmark " + benchmarkIndex + " / " + benchmarkCount + ": " + benchmark.getTitle());
    }

    @Override
    public void warmup(Benchmark benchmark, boolean finished) {
        if (finished) {
            //System.out.print("Go! ");
            logger.info("Go! ");
        } else {
            //System.out.print("Warmup... ");
            logger.info("Warmup... ");

        }
    }

    @Override
    public void progress(Benchmark benchmark, double percent) {
        //System.out.print(PERCENT_FORMAT.format(percent) + "% ");
        logger.info(PERCENT_FORMAT.format(percent) + "% ");

    }

    @Override
    public void success(BenchmarkResult result) {
        //System.out.println();
        logger.info("\n");
        //System.out.println();
        logger.info("\n");
        //System.out.println(Benchmarks.printResult(result));
        logger.info(Benchmarks.printResult(result).toString());
        //System.out.println();
        logger.info("\n");
    }

    @Override
    public void aborted(BenchmarkResult result) {
        //System.out.println();
        logger.info("\n");
        //System.out.println("Benchmark aborted: " + result.getTitle());
        logger.info("Benchmark aborted: " + result.getTitle());
        //System.out.println("Number of errors: " + result.getNumErrors());
        logger.info("Number of errors: " + result.getNumErrors());
    }

    @Override
    public void error(BenchmarkError.Type type, Exception e, BenchmarkResult result) {
        //System.out.println("Benchmark error of type: " + type);
        //e.printStackTrace();
        logger.error("Benchmark error of type: " + type, e);
    }

    @Override
    public void fatal(Exception e) {
        //System.out.println("Fatal benchmark error: " + e.getClass().getSimpleName());
        //e.printStackTrace();
        logger.error("Fatal benchmark error: " + e.getClass().getSimpleName(), e);
    }

}

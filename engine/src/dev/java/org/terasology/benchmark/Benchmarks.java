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

import com.google.common.base.Preconditions;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Benchmarks contains methods to execute one or many benchmarks with support for a progress callback as well as
 * a simple pretty printer for benchmark results.
 *
 */
public final class Benchmarks {

    private Benchmarks() {
    }

    public static BenchmarkResult execute(Benchmark benchmark, int benchmarkIndex, int benchmarkCount, BenchmarkCallback callback) {

        if (callback != null) {
            callback.begin(benchmark, benchmarkIndex, benchmarkCount);
        }

        final BenchmarkResult result = new BasicBenchmarkResult(benchmark);
        final int[] repetitions = Preconditions.checkNotNull(benchmark.getRepetitions(), "Benchmark::getRepetitions() must not return null");
        Preconditions.checkState(repetitions.length > 0, "Benchmark::getRepetitions() must return an array of size greater than zero");

        try {
            benchmark.setup();
        } catch (Exception e) {
            result.addError(BenchmarkError.Type.Setup, e);
            if (callback != null) {
                callback.error(BenchmarkError.Type.Setup, e, result);
            }
            return result;
        }

        try {
            if (callback != null) {
                callback.warmup(benchmark, false);
            }
            for (int i = 0; i < benchmark.getWarmupRepetitions(); ++i) {
                benchmark.run();
            }
            if (callback != null) {
                callback.warmup(benchmark, true);
            }
        } catch (Exception e) {
            result.addError(BenchmarkError.Type.Warmup, e);
            if (callback != null) {
                callback.error(BenchmarkError.Type.Warmup, e, result);
            }
            return result;
        }

        int repsTotal = 0;
        int repsSoFar = 0;
        for (int reps : repetitions) {
            repsTotal += reps;
        }
        int repsPart = repsTotal / 20;

        int repIndex = 0;
        boolean aborted = false;
        for (int reps : repetitions) {

            try {
                benchmark.prerun();
            } catch (Exception e) {
                aborted = true;
                result.addError(BenchmarkError.Type.PreRun, e);
                if (callback != null) {
                    callback.error(BenchmarkError.Type.PreRun, e, result);
                }
                break;
            }

            long start = time();
            long elapsed;
            try {
                result.setStartTime(repIndex, TimeUnit.MILLISECONDS.convert(start, TimeUnit.NANOSECONDS));
                int currentReps = reps;
                while (currentReps > repsPart) {
                    for (int i = 0; i < repsPart; ++i) {
                        benchmark.run();
                    }
                    currentReps -= repsPart;
                    repsSoFar += repsPart;
                    if (callback != null) {
                        callback.progress(benchmark, 100d / (double) repsTotal * (double) repsSoFar);
                    }
                }
                if (currentReps <= repsPart) {
                    for (int i = 0; i < currentReps; ++i) {
                        benchmark.run();
                    }
                    repsSoFar += currentReps;
                    if (callback != null) {
                        callback.progress(benchmark, 100d / (double) repsTotal * (double) repsSoFar);
                    }
                }
                elapsed = elapsed(start, TimeUnit.NANOSECONDS);
                result.setRunTime(repIndex, TimeUnit.MILLISECONDS.convert(elapsed, TimeUnit.NANOSECONDS));
            } catch (Exception e) {
                aborted = true;
                result.addError(BenchmarkError.Type.Run, e);
                if (callback != null) {
                    callback.error(BenchmarkError.Type.Run, e, result);
                }
                break;
            }

            try {
                benchmark.postrun();
            } catch (Exception e) {
                aborted = true;
                result.addError(BenchmarkError.Type.PostRun, e);
                if (callback != null) {
                    callback.error(BenchmarkError.Type.PostRun, e, result);
                }
                break;
            }

            ++repIndex;
        }

        try {
            benchmark.finish(aborted);
        } catch (Exception e) {
            result.addError(BenchmarkError.Type.Finish, e);
            if (callback != null) {
                callback.error(BenchmarkError.Type.Finish, e, result);
            }
        }

        if (callback != null) {
            if (result.isAborted()) {
                callback.aborted(result);
            } else {
                callback.success(result);
            }
        }

        return result;
    }

    public static List<BenchmarkResult> execute(List<Benchmark> benchmarks, BenchmarkCallback callback) {
        Preconditions.checkNotNull(benchmarks);

        final List<BenchmarkResult> results = new LinkedList<>();
        final int benchmarkCount = benchmarks.size();

        try {
            int benchmarkIndex = 1;
            for (Benchmark benchmark : benchmarks) {
                Preconditions.checkNotNull(benchmark);

                BenchmarkResult result = execute(benchmark, benchmarkIndex, benchmarkCount, callback);
                results.add(result);

                benchmarkIndex++;
            }
        } catch (Exception e) {
            if (callback != null) {
                callback.fatal(e);
            }
        }

        return results;
    }

    public static StringBuilder printResults(List<BenchmarkResult> results) {
        return printResults(results, new StringBuilder());
    }


    public static StringBuilder printResults(List<BenchmarkResult> results, StringBuilder b) {
        Preconditions.checkNotNull(b);
        final int resultCount = results.size();
        int resultIndex = 1;
        for (BenchmarkResult result : results) {
            if (resultIndex > 1) {
                b.append("\n");
            }
            b.append("Benchmark " + resultIndex + " / " + resultCount + ": " + result.getTitle() + "\n");
            printResult(result, b);
            resultIndex++;
        }
        return b;
    }

    public static StringBuilder printResult(BenchmarkResult result) {
        return printResult(result, new StringBuilder());
    }

    public static StringBuilder printResult(BenchmarkResult result, StringBuilder b) {
        Preconditions.checkNotNull(b);
        BenchmarkResult.Column<?>[] columns = getColumns(result);
        printFieldTitles(result, columns, b);
        for (int repIndex = 0; repIndex < result.getRepetitions(); repIndex++) {
            printFieldValues(result, repIndex, columns, b);
        }
        return b;
    }

    private static BenchmarkResult.Column<?>[] getColumns(BenchmarkResult result) {
        BenchmarkResult.Column<?>[] columns = new BenchmarkResult.Column<?>[result.getNumColumns()];
        Iterator<BenchmarkResult.Column<?>> it = result.getColumnsIterator();
        int i = 0;
        while (it.hasNext()) {
            columns[i++] = it.next();
        }
        return columns;
    }

    private static void printFieldTitles(BenchmarkResult result, BenchmarkResult.Column<?>[] columns, StringBuilder b) {
        boolean first = true;
        for (BenchmarkResult.Column<?> col : columns) {
            if (first) {
                first = false;
            } else {
                b.append(" | ");
            }
            b.append(col.alignment.pad(col.name, col.computeMaxWidth()));
        }
        b.append("\n");
    }

    private static void printFieldValues(BenchmarkResult result, int repIndex, BenchmarkResult.Column<?>[] columns, StringBuilder b) {
        boolean first = true;
        for (BenchmarkResult.Column<?> col : columns) {
            if (first) {
                first = false;
            } else {
                b.append(" | ");
            }
            b.append(col.alignment.pad(col.getValue(repIndex), col.computeMaxWidth()));
        }
        b.append("\n");
    }

    private static long time() {
        return System.nanoTime();
    }

    private static long elapsed(long time, TimeUnit unit) {
        Preconditions.checkNotNull(unit);
        long result = time() - time;
        return unit.convert(result, TimeUnit.NANOSECONDS);
    }

}

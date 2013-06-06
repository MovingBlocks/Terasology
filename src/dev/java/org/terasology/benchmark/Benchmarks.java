package org.terasology.benchmark;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Preconditions;

/**
 * Benchmarks contains methods to execute one or many benchmarks with support for a progress callback as well as
 * a simple pretty printer for benchmark results.
 * 
 * @author Manuel Brotz <manu.brotz@gmx.ch>
 *
 */
public final class Benchmarks {
    
    private Benchmarks() {} 
    
    public static BenchmarkResult execute(Benchmark benchmark, int benchmarkIndex, int benchmarkCount, BenchmarkCallback callback) {
        
        if (callback != null)
            callback.begin(benchmark, benchmarkIndex, benchmarkCount);

        final BenchmarkResult result = Preconditions.checkNotNull(benchmark.createResult(), "Benchmark::createResult() must not return null");
        final int[] repetitions = Preconditions.checkNotNull(benchmark.getRepetitions(), "Benchmark::getRepetitions() must not return null");
        Preconditions.checkState(repetitions.length > 0, "Benchmark::getRepetitions() must return an array of size greater than zero");
        
        try {
            benchmark.setup();
        } catch (Exception e) {
            result.addError(BenchmarkError.Type.Setup, e);
            if (callback != null) callback.error(BenchmarkError.Type.Setup, e, result);
            return result;
        }
        
        try {
            if (callback != null) callback.warmup(benchmark, false);
            benchmark.run(-1, benchmark.getWarmupRepetitions(), result);
            if (callback != null) callback.warmup(benchmark, true);
        } catch (Exception e) {
            result.addError(BenchmarkError.Type.Warmup, e);
            if (callback != null) callback.error(BenchmarkError.Type.Warmup, e, result);
            return result;
        }
        
        int repsTotal = 0, repsSoFar = 0;
        for (int reps : repetitions) {
            repsTotal += reps;
        }
        int repsPart = repsTotal / 20;
        
        int repIndex = 0;
        boolean aborted = false;
        for (int reps : repetitions) {
            
            try {
                benchmark.prerun(repIndex);
            } catch (Exception e) {
                aborted = true;
                result.addError(BenchmarkError.Type.PreRun, e);
                if (callback != null) callback.error(BenchmarkError.Type.PreRun, e, result);
                break;
            }
            
            long start = time(), elapsed = 0;
            try {
                result.setStartTime(repIndex, TimeUnit.MILLISECONDS.convert(start, TimeUnit.NANOSECONDS));
                while (reps > repsPart) {
                    benchmark.run(repIndex, repsPart, result);
                    reps -= repsPart;
                    repsSoFar += repsPart;
                    if (callback != null) callback.progress(benchmark, 100d / (double)repsTotal * (double)repsSoFar);
                    elapsed += elapsed(start, TimeUnit.NANOSECONDS);
                    start = time();
                }
                if (reps <= repsPart) {
                    benchmark.run(repIndex, reps, result);
                    repsSoFar += reps;
                    if (callback != null) callback.progress(benchmark, 100d / (double)repsTotal * (double)repsSoFar);
                    elapsed += elapsed(start, TimeUnit.NANOSECONDS);
                }
                result.setRunTime(repIndex, TimeUnit.MILLISECONDS.convert(elapsed, TimeUnit.NANOSECONDS));
            } catch (Exception e) {
                aborted = true;
                result.addError(BenchmarkError.Type.Run, e);
                if (callback != null) callback.error(BenchmarkError.Type.Run, e, result);
                break;
            }
                        
            try {
                benchmark.postrun(repIndex, result);
            } catch (Exception e) {
                aborted = true;
                result.addError(BenchmarkError.Type.PostRun, e);
                if (callback != null) callback.error(BenchmarkError.Type.PostRun, e, result);
                break;
            }
            
            ++repIndex;
        }
        
        try {
            benchmark.finish(aborted); 
        } catch (Exception e) {
            result.addError(BenchmarkError.Type.Finish, e);
            if (callback != null) callback.error(BenchmarkError.Type.Finish, e, result);
        }

        if (callback != null)
            if (result.isAborted()) {
                callback.aborted(result);
            } else {
                callback.success(result);
            }
        
        return result;
    }
    
    public static List<BenchmarkResult> execute(List<Benchmark> benchmarks, BenchmarkCallback callback) {
        Preconditions.checkNotNull(benchmarks);
        
        final List<BenchmarkResult> results = new LinkedList<BenchmarkResult>();
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
            if (callback != null)
                callback.fatal(e);
        }
        
        return results;
    }
    
    public static StringBuilder printResults(List<BenchmarkResult> results, StringBuilder b) {
        if (b == null) b = new StringBuilder();
        final int resultCount = results.size();
        int resultIndex = 1;
        for (BenchmarkResult result : results) {
            if (resultIndex > 1) b.append("\n");
            b.append("Benchmark " + resultIndex + " / " + resultCount + ": " + result.getTitle() + "\n");
            printResult(result, b);
            resultIndex++;
        }
        return b;
    }
    
    public static StringBuilder printResult(BenchmarkResult result, StringBuilder b) {
        if (b == null) b = new StringBuilder();
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
            if (first) first = false; else b.append(" | ");
            b.append(col.alignment.pad(col.name, col.computeMaxWidth()));
        }
        b.append("\n");
    }
    
    private static void printFieldValues(BenchmarkResult result, int repIndex, BenchmarkResult.Column<?>[] columns, StringBuilder b) {
        boolean first = true;
        for (BenchmarkResult.Column<?> col : columns) {
            if (first) first = false; else b.append(" | ");
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

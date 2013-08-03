package org.terasology.benchmark;

/**
 * BenchmarkCallback allows to watch the progress of the execution of one or many benchmarks.
 * 
 * @author Manuel Brotz <manu.brotz@gmx.ch>
 *
 */
public interface BenchmarkCallback {
    
    void begin(Benchmark benchmark, int benchmarkIndex, int benchmarkCount);

    void warmup(Benchmark benchmark, boolean finished);
    
    void progress(Benchmark benchmark, double percent);
    
    void success(BenchmarkResult result);
    
    void aborted(BenchmarkResult result);
    
    void error(BenchmarkError.Type type, Exception e, BenchmarkResult result);
    
    void fatal(Exception e);

}

package org.terasology.benchmark;

/**
 * BenchmarkCallback allows to watch the progress of the execution of one or many benchmarks.
 * 
 * @author Manuel Brotz <manu.brotz@gmx.ch>
 *
 */
public interface BenchmarkCallback {
    
    public void begin(Benchmark benchmark, int benchmarkIndex, int benchmarkCount);

    public void warmup(Benchmark benchmark, boolean finished);
    
    public void progress(Benchmark benchmark, double percent);
    
    public void success(BenchmarkResult result);
    
    public void aborted(BenchmarkResult result);
    
    public void error(BenchmarkError.Type type, Exception e, BenchmarkResult result);
    
    public void fatal(Exception e);

}

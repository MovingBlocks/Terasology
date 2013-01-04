package org.terasology.benchmark;

/**
 * Benchmark is an abstract class which is used to implement one particular benchmark.
 * 
 * @author Manuel Brotz <manu.brotz@gmx.ch>
 *
 */
public abstract class Benchmark {

    public Benchmark() {}

    public abstract String getTitle();
    
    public abstract int getWarmupRepetitions();
    
    public abstract int[] getRepetitions();
    
    public abstract BenchmarkResult createResult();
    
    public abstract void setup();
    
    public abstract void prerun(int index);
    
    public abstract int run(int index, int repetitions, BenchmarkResult result);
    
    public abstract void postrun(int index, BenchmarkResult result);
    
    public abstract void finish(boolean aborted);
}

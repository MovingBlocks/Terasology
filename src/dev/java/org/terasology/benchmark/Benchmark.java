package org.terasology.benchmark;

/**
 * Benchmark is an abstract class which is used to implement one particular benchmark.
 * 
 * @author Manuel Brotz <manu.brotz@gmx.ch>
 *
 */
public interface Benchmark {

    String getTitle();
    
    int getWarmupRepetitions();
    
    int[] getRepetitions();

    void setup();
    
    void prerun();
    
    void run();
    
    void postrun();
    
    void finish(boolean aborted);
}

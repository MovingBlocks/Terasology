package org.terasology.benchmark;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * PrintToConsoleCallback implements BenchmarkCallback and simply prints everything to the console.
 * 
 * @author Manuel Brotz <manu.brotz@gmx.ch>
 *
 */
public class PrintToConsoleCallback implements BenchmarkCallback {
    
    private static final NumberFormat pf = new DecimalFormat("##0.0");

    @Override
    public void begin(Benchmark benchmark, int benchmarkIndex, int benchmarkCount) {
        System.out.println("Benchmark " + benchmarkIndex + " / " + benchmarkCount + ": " + benchmark.getTitle());
    }

    @Override
    public void warmup(Benchmark benchmark, boolean finished) {
        if (finished) 
            System.out.print("Go! ");
        else
            System.out.print("Warmup... ");
    }

    @Override
    public void progress(Benchmark benchmark, double percent) {
        System.out.print(pf.format(percent) + "% ");
    }

    @Override
    public void success(BenchmarkResult result) {
        System.out.println();
        System.out.println();
        System.out.println(Benchmarks.printResult(result, null));
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
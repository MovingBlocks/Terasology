package org.terasology.benchmark;


/**
 * BasicBenchmarkResult extends BenchmarkResult and adds three basic columns for pretty printing.
 * 
 * @author Manuel Brotz <manu.brotz@gmx.ch>
 *
 */
public class BasicBenchmarkResult extends BenchmarkResult {

    public BasicBenchmarkResult(Benchmark benchmark) {
        super(benchmark);
        addColumn(new Column<BasicBenchmarkResult>(this, Alignment.Right, "#") {
            @Override
            public String getValueInternal(int rep) {
                return String.valueOf(rep+1);
            }
        });
        addColumn(new Column<BasicBenchmarkResult>(this, Alignment.Right, "Repetitions") {
            @Override
            protected String getValueInternal(int rep) {
                return String.valueOf(owner.getRepetitions(rep));
            }
        });
        addColumn(new Column<BasicBenchmarkResult>(this, Alignment.Right, "Time in ms") {
            @Override
            protected String getValueInternal(int rep) {
                return String.valueOf(owner.getRunTime(rep));
            }
        });
    }

}

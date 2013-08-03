package org.terasology.benchmark;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import java.util.Iterator;
import java.util.List;

/**
 * BenchmarkResult records the results and the errors of the execution of one particular benchmark.
 * It also maintains a list of columns which are very useful for pretty printing the results.
 *
 * @author Manuel Brotz <manu.brotz@gmx.ch>
 */
public abstract class BenchmarkResult {

    private final List<Column<?>> columns = Lists.newLinkedList();

    private final Class<?> benchmarkClass;
    private final String title;
    private final int[] repetitions;
    private final long[] starttime;
    private final long[] runtime;
    private boolean aborted;
    private final List<BenchmarkError> errors = Lists.newLinkedList();

    public static enum Alignment {
        LEFT {
            @Override
            public String pad(String value, int size) {
                String result = (value == null ? "" : value);
                while (result.length() < size) {
                    result = result + " ";
                }
                return result;
            }
        },

        RIGHT {
            @Override
            public String pad(String value, int size) {
                String result = (value == null ? "" : value);
                while (result.length() < size) {
                    result = " " + result;
                }
                return result;
            }
        };

        public abstract String pad(String value, int size);
    }

    public abstract static class Column<T extends BenchmarkResult> {

        private final int reps;
        private final String[] cache;
        private boolean hasMaxWidth = false;
        private int maxWidth = 0;

        public final T owner;
        public final Alignment alignment;
        public final String name;

        protected abstract String getValueInternal(int rep);

        public Column(T owner, Alignment alignment, String name) {
            this.owner = Preconditions.checkNotNull(owner);
            this.alignment = Preconditions.checkNotNull(alignment);
            this.name = Preconditions.checkNotNull(name);
            this.reps = owner.getRepetitions();
            this.cache = new String[reps];
        }

        public final String getValue(int rep) {
            Preconditions.checkElementIndex(rep, reps, "Parameter 'rep'");
            if (cache[rep] == null) {
                String v = Preconditions.checkNotNull(getValueInternal(rep), "BenchmarkResult::Column::getValueInternal() must never return null (rep=" + rep + ")");
                cache[rep] = v;
                return v;
            }
            return cache[rep];
        }

        public final int computeMaxWidth() {
            if (!hasMaxWidth) {
                int max = name.length();
                for (int i = 0; i < reps; i++) {
                    String v = getValue(i);
                    if (v.length() > max) {
                        max = v.length();
                    }
                }
                hasMaxWidth = true;
                maxWidth = max;
            }
            return maxWidth;
        }
    }

    public BenchmarkResult(Benchmark benchmark) {
        Preconditions.checkNotNull(benchmark, "Parameter 'benchmark' must not be null");
        this.benchmarkClass = benchmark.getClass();
        this.title = benchmark.getTitle();
        this.repetitions = Preconditions.checkNotNull(benchmark.getRepetitions());
        Preconditions.checkArgument(repetitions.length > 0, "The length of the parameter 'repetitions' has to be greater than zero");
        int reps = repetitions.length;
        this.starttime = new long[reps];
        this.runtime = new long[reps];
    }

    public final Iterator<Column<?>> getColumnsIterator() {
        return columns.iterator();
    }

    public final void addColumn(Column<?> column) {
        columns.add(Preconditions.checkNotNull(column));
    }

    public final int getNumColumns() {
        return columns.size();
    }

    public final Class<?> getBenchmarkClass() {
        return benchmarkClass;
    }

    public final String getTitle() {
        return title;
    }

    public final int getRepetitions() {
        return repetitions.length;
    }

    public final int getRepetitions(int rep) {
        return repetitions[rep];
    }

    public final long getStartTime(int rep) {
        return starttime[rep];
    }

    public final void setStartTime(int rep, long value) {
        starttime[rep] = value;
    }

    public final long getRunTime(int rep) {
        return runtime[rep];
    }

    public final void setRunTime(int rep, long value) {
        runtime[rep] = value;
    }

    public final boolean isAborted() {
        return aborted;
    }

    public final Iterator<BenchmarkError> getErrorsIterator() {
        return errors.iterator();
    }

    public final int getNumErrors() {
        return errors.size();
    }

    public final void addError(BenchmarkError.Type type, Exception error) {
        this.aborted = this.aborted || Preconditions.checkNotNull(type).abort;
        errors.add(new BenchmarkError(type, error));
    }

}

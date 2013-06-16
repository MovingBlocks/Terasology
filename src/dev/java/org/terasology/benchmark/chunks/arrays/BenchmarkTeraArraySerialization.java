package org.terasology.benchmark.chunks.arrays;

import org.terasology.benchmark.BasicBenchmarkResult;
import org.terasology.benchmark.Benchmark;
import org.terasology.benchmark.BenchmarkResult;
import org.terasology.world.chunks.perBlockStorage.TeraArray;

import com.google.common.base.Preconditions;

@SuppressWarnings("rawtypes")
public abstract class BenchmarkTeraArraySerialization extends Benchmark {

    public static final int BUFFER_SIZE = 1024 * 1024;
    
    public final TeraArray.SerializationHandler handler;
    public final TeraArray array;
    
    @SuppressWarnings("unchecked")
    public BenchmarkTeraArraySerialization(TeraArray.SerializationHandler handler, TeraArray array) {
        this.handler = Preconditions.checkNotNull(handler);
        this.array = Preconditions.checkNotNull(array);
        Preconditions.checkArgument(handler.canHandle(array.getClass()), "The supplied serialization handler is incompatible to the supplied array");
    }

    @Override
    public int getWarmupRepetitions() {
        return 10000;
    }

    @Override
    public int[] getRepetitions() {
        return new int[] {1000, 5000, 10000};
    }

    @Override
    public BenchmarkResult createResult() {
        return new BasicBenchmarkResult(this);
    }

}

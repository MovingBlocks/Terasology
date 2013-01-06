package org.terasology.benchmark.chunks.arrays;

import java.io.ByteArrayOutputStream;
import java.util.LinkedList;
import java.util.List;

import org.terasology.benchmark.Benchmark;
import org.terasology.benchmark.PrintToConsoleCallback;
import org.terasology.benchmark.Benchmarks;
import org.terasology.utilities.FastRandom;
import org.terasology.world.chunks.blockdata.TeraArray;
import org.terasology.world.chunks.blockdata.TeraDenseArray16Bit;
import org.terasology.world.chunks.blockdata.TeraDenseArray4Bit;
import org.terasology.world.chunks.blockdata.TeraDenseArray8Bit;
import org.terasology.world.chunks.blockdata.TeraSparseArray4Bit;
import org.terasology.world.chunks.blockdata.TeraSparseArray8Bit;

/**
 * TeraArraysBenchmark simplifies the execution of the benchmarks for tera arrays.
 * 
 * @author Manuel Brotz <manu.brotz@gmx.ch>
 *
 */
@SuppressWarnings("unused")
public final class TeraArraysBenchmark {
 
    private TeraArraysBenchmark() {}
    
    private static final byte[][] inflated8Bit = new byte[256][];
    private static final byte[] deflated8Bit = new byte[256];
    
    private static final byte[][] inflated4Bit = new byte[256][];
    private static final byte[] deflated4Bit = new byte[256];

    static {
        for (int i = 0; i < inflated8Bit.length; i++) {
            inflated8Bit[i] = new byte[256];
        }
        for (int i = 0; i < inflated4Bit.length; i++) {
            inflated4Bit[i] = new byte[128];
        }
    }
    
    public static void main(String[] args) {

        final List<Benchmark> benchmarks = new LinkedList<Benchmark>();
        
//        benchmarks.add(new BenchmarkTeraArraySerializeObject(new TeraDenseArray8Bit.SerializationHandler(), new TeraDenseArray8Bit(16, 256, 16)));
//        benchmarks.add(new BenchmarkTeraArraySerializeToBuffer(new TeraDenseArray8Bit.SerializationHandler(), new TeraDenseArray8Bit(16, 256, 16)));
//        benchmarks.add(new BenchmarkTeraArraySerializeToStreamViaByteArray(new TeraDenseArray8Bit.SerializationHandler(), new TeraDenseArray8Bit(16, 256, 16)));
//        benchmarks.add(new BenchmarkTeraArraySerializeToStreamViaChannel(new TeraDenseArray8Bit.SerializationHandler(), new TeraDenseArray8Bit(16, 256, 16)));
//
//        benchmarks.add(new BenchmarkTeraArrayDeserializeFromBuffer(new TeraDenseArray8Bit.SerializationHandler(), new TeraDenseArray8Bit(16, 256, 16)));
//
//
//        benchmarks.add(new BenchmarkTeraArrayRead(new TeraDenseArray8Bit(16, 256, 16)));
//        benchmarks.add(new BenchmarkTeraArrayRead(new TeraDenseArray4Bit(16, 256, 16)));
//        benchmarks.add(new BenchmarkTeraArrayRead(new TeraSparseArray8Bit(16, 256, 16, inflated8Bit, deflated8Bit)));
//        benchmarks.add(new BenchmarkTeraArrayRead(new TeraSparseArray4Bit(16, 256, 16, inflated4Bit, deflated4Bit)));
//
//
//        benchmarks.add(new BenchmarkTeraArrayWrite(new TeraDenseArray8Bit(16, 256, 16)));
//        benchmarks.add(new BenchmarkTeraArrayWrite(new TeraDenseArray4Bit(16, 256, 16)));
//        benchmarks.add(new BenchmarkTeraArrayWrite(new TeraSparseArray8Bit(16, 256, 16, inflated8Bit, deflated8Bit)));
//        benchmarks.add(new BenchmarkTeraArrayWrite(new TeraSparseArray4Bit(16, 256, 16, inflated4Bit, deflated4Bit)));

        Benchmarks.execute(benchmarks, new PrintToConsoleCallback());
        
    }
}

// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.benchmark.chunks.arrays;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.terasology.engine.world.chunks.blockdata.TeraArray;
import org.terasology.engine.world.chunks.blockdata.TeraArray.SerializationHandler;
import org.terasology.engine.world.chunks.blockdata.TeraDenseArray16Bit;
import org.terasology.engine.world.chunks.blockdata.TeraDenseArray4Bit;
import org.terasology.engine.world.chunks.blockdata.TeraDenseArray8Bit;
import org.terasology.engine.world.chunks.blockdata.TeraSparseArray4Bit;
import org.terasology.engine.world.chunks.blockdata.TeraSparseArray8Bit;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 1)
@Fork(1)
@Measurement(iterations = 1)
public class TeraArrayBenchmark {

    public static final int BUFFER_SIZE = 1024 * 1024;

    private static final byte[][] INFLATED_8_BIT = new byte[256][];
    private static final byte[] DEFLATED_8_BIT = new byte[256];

    private static final byte[][] INFLATED_4_BIT = new byte[256][];
    private static final byte[] DEFLATED_4_BIT = new byte[256];

    static {
        for (int i = 0; i < INFLATED_8_BIT.length; i++) {
            INFLATED_8_BIT[i] = new byte[256];
        }
        for (int i = 0; i < INFLATED_4_BIT.length; i++) {
            INFLATED_4_BIT[i] = new byte[128];
        }
    }


    @Benchmark
    public int fullyRead(ArrayState state) {
        int tmp = 0;
        for (int y = 0; y < state.array.getSizeY(); y++) {
            for (int z = 0; z < state.array.getSizeZ(); z++) {
                for (int x = 0; x < state.array.getSizeX(); x++) {
                    tmp += state.array.get(x, y, z);
                }
            }
        }
        return tmp;
    }

    @Benchmark
    public void fullyWrite(ArrayState state) {
        for (int y = 0; y < state.array.getSizeY(); y++) {
            for (int z = 0; z < state.array.getSizeZ(); z++) {
                for (int x = 0; x < state.array.getSizeX(); x++) {
                    state.array.set(x, y, z, 1);
                }
            }
        }
    }

    @Benchmark
    public ByteBuffer toByteBuffer(ArrayState state, ByteBufferState bbState) {
        return state.handler.serialize(state.array, bbState.out);
    }

    @Benchmark
    public TeraArray fromByteBuffer(ArrayState state, FilledByteBufferState bbState) {
        return state.handler.deserialize(bbState.in);
    }

    public enum TeraArrayType {
        DENCE_4BIT(() -> new TeraDenseArray4Bit(16, 256, 16), TeraDenseArray4Bit.SerializationHandler::new),
        DENCE_8BIT(() -> new TeraDenseArray8Bit(16, 256, 16), TeraDenseArray8Bit.SerializationHandler::new),
        DENCE_16BIT(() -> new TeraDenseArray16Bit(16, 256, 16), TeraDenseArray16Bit.SerializationHandler::new),
        SPARCE_4BIT(() -> new TeraSparseArray4Bit(16, 256, 16, INFLATED_4_BIT, DEFLATED_4_BIT),
                TeraSparseArray4Bit.SerializationHandler::new),
        SPARCE_8BIT(() -> new TeraSparseArray8Bit(16, 256, 16, INFLATED_8_BIT, DEFLATED_8_BIT),
                TeraSparseArray8Bit.SerializationHandler::new);

        private final Supplier<TeraArray> creator;
        private final Supplier<SerializationHandler> handler;

        TeraArrayType(Supplier<TeraArray> creator, Supplier<SerializationHandler> handler) {
            this.creator = creator;
            this.handler = handler;
        }

        public TeraArray create() {
            return creator.get();
        }

        public SerializationHandler handler() {
            return handler.get();
        }
    }


    @State(Scope.Thread)
    public static class ByteBufferState {
        private ByteBuffer out;

        @Setup(Level.Invocation)
        public void setup() throws IOException {
            out = ByteBuffer.allocate(BUFFER_SIZE);
        }

    }

    @State(Scope.Thread)
    public static class FilledByteBufferState {
        private ByteBuffer in;

        @Setup(Level.Invocation)
        public void setup(ArrayState arrayState) {
            in = ByteBuffer.allocate(BUFFER_SIZE);
            arrayState.handler.serialize(arrayState.array, in);
            in.rewind();
        }

    }

    @State(Scope.Thread)
    public static class ArrayState {
        @Param({"DENCE_4BIT", "DENCE_8BIT", "DENCE_16BIT", "SPARCE_4BIT", "SPARCE_8BIT"})
        private static TeraArrayType arrayType;

        private SerializationHandler handler;
        private TeraArray array;

        @Setup
        public void setup() {
            array = arrayType.create();
            handler = arrayType.handler();
        }
    }

}

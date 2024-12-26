// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.benchmark.chunks.arrays;

import org.openjdk.jmh.annotations.*;
import org.terasology.engine.world.chunks.blockdata.TeraArray;
import org.terasology.engine.world.chunks.blockdata.TeraArray.SerializationHandler;
import org.terasology.engine.world.chunks.blockdata.TeraDenseArray16Bit;
import org.terasology.engine.world.chunks.blockdata.TeraDenseArray4Bit;
import org.terasology.engine.world.chunks.blockdata.TeraDenseArray8Bit;
import org.terasology.engine.world.chunks.blockdata.TeraSparseArray4Bit;
import org.terasology.engine.world.chunks.blockdata.TeraSparseArray8Bit;

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

    private static final byte[][] INFLATED_8_BIT = new byte[256][256];
    private static final byte[][] INFLATED_4_BIT = new byte[256][128];
    private static final byte[] DEFLATED_8_BIT = new byte[256];
    private static final byte[] DEFLATED_4_BIT = new byte[256];

    @Benchmark
    public int fullyRead(ArrayState state) {
        TeraArray array = state.array;
        int sizeX = array.getSizeX();
        int sizeY = array.getSizeY();
        int sizeZ = array.getSizeZ();

        int tmp = 0;
        for (int y = 0; y < sizeY; y++) {
            for (int z = 0; z < sizeZ; z++) {
                for (int x = 0; x < sizeX; x++) {
                    tmp += array.get(x, y, z);
                }
            }
        }
        return tmp;
    }

    @Benchmark
    public void fullyWrite(ArrayState state) {
        TeraArray array = state.array;
        int sizeX = array.getSizeX();
        int sizeY = array.getSizeY();
        int sizeZ = array.getSizeZ();

        for (int y = 0; y < sizeY; y++) {
            for (int z = 0; z < sizeZ; z++) {
                for (int x = 0; x < sizeX; x++) {
                    array.set(x, y, z, 1);
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
        DENSE_4BIT(() -> new TeraDenseArray4Bit(16, 256, 16), TeraDenseArray4Bit.SerializationHandler::new),
        DENSE_8BIT(() -> new TeraDenseArray8Bit(16, 256, 16), TeraDenseArray8Bit.SerializationHandler::new),
        DENSE_16BIT(() -> new TeraDenseArray16Bit(16, 256, 16), TeraDenseArray16Bit.SerializationHandler::new),
        SPARSE_4BIT(() -> new TeraSparseArray4Bit(16, 256, 16, INFLATED_4_BIT, DEFLATED_4_BIT), TeraSparseArray4Bit.SerializationHandler::new),
        SPARSE_8BIT(() -> new TeraSparseArray8Bit(16, 256, 16, INFLATED_8_BIT, DEFLATED_8_BIT), TeraSparseArray8Bit.SerializationHandler::new);

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
        ByteBuffer out;

        @Setup(Level.Invocation)
        public void setup() {
            out = ByteBuffer.allocateDirect(BUFFER_SIZE);
        }
    }

    @State(Scope.Thread)
    public static class FilledByteBufferState {
        ByteBuffer in;

        @Setup(Level.Invocation)
        public void setup(ArrayState arrayState) {
            in = ByteBuffer.allocateDirect(BUFFER_SIZE);
            arrayState.handler.serialize(arrayState.array, in);
            in.flip();
        }
    }

    @State(Scope.Thread)
    public static class ArrayState {
        @Param({"DENSE_4BIT", "DENSE_8BIT", "DENSE_16BIT", "SPARSE_4BIT", "SPARSE_8BIT"})
        private static TeraArrayType arrayType;

        SerializationHandler handler;
        TeraArray array;

        @Setup
        public void setup() {
            array = arrayType.create();
            handler = arrayType.handler();
        }
    }
}

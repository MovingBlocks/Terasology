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
import org.terasology.engine.world.chunks.blockdata.TeraDenseArray16Bit;
import org.terasology.engine.world.chunks.blockdata.TeraDenseArray4Bit;
import org.terasology.engine.world.chunks.blockdata.TeraDenseArray8Bit;
import org.terasology.engine.world.chunks.blockdata.TeraOcTree;
import org.terasology.engine.world.chunks.blockdata.TeraSparseArray4Bit;
import org.terasology.engine.world.chunks.blockdata.TeraSparseArray8Bit;

import java.nio.ByteBuffer;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 1)
@Fork(1)
@Measurement(iterations = 1)
public class TeraArrayBenchmark {

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
    public int singleWriteEmpty(ArrayState state) {
        return state.array.set(0, 0, 0, 1);
    }

    @Benchmark
    public int singleReadEmpty(ArrayState state) {
        return state.array.get(0, 0, 0);
    }


    @Benchmark
    public int singleWrite(ArrayState state, RandomFilledState rnd) {
        return state.array.set(rnd.x(), rnd.y(), rnd.y(), rnd.index);
    }

    @Benchmark
    public int singleRead(ArrayState state, RandomFilledState rnd) {
        return state.array.get(rnd.x(), rnd.y(), rnd.z());
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
        DENCE_4BIT(() -> new TeraDenseArray4Bit(32, 32, 32), TeraDenseArray4Bit.SerializationHandler::new),
        DENCE_8BIT(() -> new TeraDenseArray8Bit(32, 32, 32), TeraDenseArray8Bit.SerializationHandler::new),
        DENCE_16BIT(() -> new TeraDenseArray16Bit(32, 32, 32), TeraDenseArray16Bit.SerializationHandler::new),
        SPARCE_4BIT(() -> new TeraSparseArray4Bit(32, 32, 32),
                TeraSparseArray4Bit.SerializationHandler::new),
        SPARCE_8BIT(() -> new TeraSparseArray8Bit(32, 32, 32),
                TeraSparseArray8Bit.SerializationHandler::new),
        OC_TREE(() -> new TeraOcTree((byte) 32), TeraOcTree.SerializationHandler::new);

        private final Supplier<TeraArray> creator;
        private final Supplier<TeraArray.SerializationHandler> handler;

        TeraArrayType(Supplier<TeraArray> creator, Supplier<TeraArray.SerializationHandler> handler) {
            this.creator = creator;
            this.handler = handler;
        }

        public TeraArray create() {
            return creator.get();
        }

        public TeraArray.SerializationHandler handler() {
            return handler.get();
        }
    }


    @State(Scope.Thread)
    public static class ByteBufferState {
        private ByteBuffer out;

        @Setup(Level.Invocation)
        public void setup(ArrayState arrayState)  {
            out = ByteBuffer.allocate(arrayState.handler.computeMinimumBufferSize(arrayState.array));
        }

    }

    @State(Scope.Thread)
    public static class RandomFilledState {
        public static final int COUNT = 100;
        int index;
        int[] x = new int[COUNT];
        int[] y = new int[COUNT];
        int[] z = new int[COUNT];

        @Setup(Level.Iteration)
        public void setupThread(ArrayState arrayState) {
            Random random = new Random();

            for (int i = 0; i < random.nextInt(1000); i++) {
                arrayState.array.set(random.nextInt(32),
                        random.nextInt(32),
                        random.nextInt(32),
                        random.nextInt(32)
                );
            }

            for (int i = 0; i < COUNT; i++) {
                x[i] = random.nextInt(32);
                y[i] = random.nextInt(32);
                z[i] = random.nextInt(32);
            }
        }

        @Setup(Level.Invocation)
        public void setup() {
            Random random = new Random();
            index = random.nextInt(COUNT);
        }

        public int x() {
            return x[index];
        }

        public int y() {
            return y[index];
        }

        public int z() {
            return z[index];
        }
    }

    @State(Scope.Thread)
    public static class FilledByteBufferState {
        private ByteBuffer in;

        @Setup(Level.Invocation)
        public void setup(ArrayState arrayState) {
            in = arrayState.handler.serialize(arrayState.array);
            in.rewind();
        }

    }

    @State(Scope.Thread)
    public static class ArrayState {
        @Param({"DENCE_4BIT", "DENCE_8BIT", "DENCE_16BIT", "SPARCE_4BIT", "SPARCE_8BIT", "OC_TREE"})
        private static TeraArrayType arrayType;

        private TeraArray.SerializationHandler handler;
        private TeraArray array;

        @Setup
        public void setup() {
            array = arrayType.create();
            handler = arrayType.handler();
        }
    }

}

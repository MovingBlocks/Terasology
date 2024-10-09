// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.benchmark.typehandlerlibrary;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
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
import org.reflections.Reflections;
import org.terasology.engine.persistence.typeHandling.TypeHandlerLibraryImpl;
import org.terasology.engine.persistence.typeHandling.gson.GsonPersistedDataReader;
import org.terasology.engine.persistence.typeHandling.gson.GsonPersistedDataSerializer;
import org.terasology.engine.persistence.typeHandling.gson.GsonPersistedDataWriter;
import org.terasology.engine.persistence.typeHandling.protobuf.ProtobufDataReader;
import org.terasology.engine.persistence.typeHandling.protobuf.ProtobufDataWriter;
import org.terasology.engine.persistence.typeHandling.protobuf.ProtobufPersistedDataSerializer;
import org.terasology.persistence.serializers.Serializer;
import org.terasology.persistence.typeHandling.TypeHandlerLibrary;
import org.terasology.persistence.typeHandling.bytebuffer.ByteBufferDataReader;
import org.terasology.persistence.typeHandling.bytebuffer.ByteBufferDataWriter;
import org.terasology.persistence.typeHandling.bytebuffer.ByteBufferPersistedSerializer;
import org.terasology.persistence.typeHandling.inMemory.InMemoryPersistedDataSerializer;
import org.terasology.persistence.typeHandling.inMemory.InMemoryReader;
import org.terasology.persistence.typeHandling.inMemory.InMemoryWriter;
import org.terasology.reflection.TypeInfo;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 1)
@Fork(1)
@Measurement(iterations = 1)
public class THLBenchmark {


    @Benchmark
    public Optional<byte[]> ser(SerializerState state, Data data) {
        return state.serializer.serialize(data.value, data.type);
    }

    @Benchmark
    public Optional serde(SerializerState state, Data data) {
        Optional<byte[]> o = state.serializer.serialize(data.value, data.type);
        return state.serializer.deserialize(data.type, o.get());
    }


    public enum Serializers {
        GSON((thl) -> {
            Gson gson = new Gson();
            return new Serializer(thl, new GsonPersistedDataSerializer(), new GsonPersistedDataWriter(gson),
                    new GsonPersistedDataReader(gson));
        }),
        IN_MEMORY((thl) -> new Serializer(thl, new InMemoryPersistedDataSerializer(), new InMemoryWriter(),
                new InMemoryReader())),
        PROTOBUF((thl) -> new Serializer(thl, new ProtobufPersistedDataSerializer(), new ProtobufDataWriter(),
                new ProtobufDataReader())),
        BYTEBUFFER((thl) -> new Serializer(thl, new ByteBufferPersistedSerializer(), new ByteBufferDataWriter(),
                new ByteBufferDataReader()));
        private final Function<TypeHandlerLibrary, Serializer> creator;

        Serializers(Function<TypeHandlerLibrary, Serializer> creator) {
            this.creator = creator;
        }
    }

    public enum Datas {
        BOOLEAN(Boolean.TYPE, true),
        BYTE(Byte.TYPE, (byte) 1),
        INT(Integer.TYPE, 1),
        LONG(Long.TYPE, 1L),
        FLOAT(Float.TYPE, 1F),
        DOUBLE(Double.TYPE, 1D),
        CHAR(Character.TYPE, 'c'),
        STRING(String.class, "string"),
        BO_ARRAY(boolean[].class, new boolean[]{true}),
        BY_ARRAY(byte[].class, new byte[]{(byte) 1}),
        I_ARRAY(int[].class, new int[]{1}),
        L_ARRAY(long[].class, new long[]{1L}),
        F_ARRAY(float[].class, new float[]{1F}),
        D_ARRAY(double[].class, new double[]{1D}),
        C_ARRAY(char[].class, new char[]{'c'}),
        S_ARRAY(String[].class, new String[]{"foo", "bar"}),
        ENUM(ExampleEnum.class, ExampleEnum.ONE),
        S_LIST(new TypeInfo<List<String>>() {
        }, Lists.newArrayList("foo", "bar")),
        S_SET(new TypeInfo<Set<String>>() {
        }, Sets.newHashSet("foo", "bar")),
        E_SET(new TypeInfo<EnumSet<ExampleEnum>>() {
        }, EnumSet.of(ExampleEnum.ONE, ExampleEnum.THREE));

        private final TypeInfo type;
        private final Object obj;

        Datas(Class clazz, Object obj) {
            this.type = TypeInfo.of(clazz);
            this.obj = obj;
        }

        Datas(TypeInfo type, Object obj) {
            this.type = type;
            this.obj = obj;
        }
    }

    enum ExampleEnum {
        ONE,
        TWO,
        THREE
    }

    @State(Scope.Thread)
    public static class Data {
        @Param({"BOOLEAN",
//                "BYTE",
                "INT",
//                "LONG",
//                "FLOAT",
//                "DOUBLE",
//                "CHAR",
//                "STRING",
                "BO_ARRAY",
                "BY_ARRAY",
                "I_ARRAY",
//                "L_ARRAY",
//                "F_ARRAY",
//                "D_ARRAY",
//                "C_ARRAY",
                "S_ARRAY",
//                "ENUM",
                "S_LIST",
//                "S_SET",
                "E_SET"})
        private static Datas datas;

        TypeInfo type;
        Object value;

        @Setup(Level.Iteration)
        public void setup() {
            type = datas.type;
            value = datas.obj;
        }
    }

    @State(Scope.Benchmark)
    public static class THLState {
        private static Reflections reflections;
        private static TypeHandlerLibrary typeHandlerLibrary;

        @Setup(Level.Trial)
        public static void setup() {
            reflections = new Reflections(THLBenchmark.class.getClassLoader());
            typeHandlerLibrary = TypeHandlerLibraryImpl.withReflections(reflections);
        }
    }

    @State(Scope.Benchmark)
    public static class SerializerState {
        @Param({"GSON", "IN_MEMORY", "PROTOBUF", "BYTEBUFFER"})
        private static Serializers serializers;

        private Serializer serializer;

        @Setup(Level.Trial)
        public void setup(THLState state) {
            serializer = serializers.creator.apply(THLState.typeHandlerLibrary);
        }
    }


}

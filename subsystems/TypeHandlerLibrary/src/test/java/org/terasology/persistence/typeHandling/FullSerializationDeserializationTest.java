// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.persistence.typeHandling;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.collect.Streams;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.persistence.serializers.Serializer;
import org.terasology.persistence.typeHandling.bytebuffer.ByteBufferDataReader;
import org.terasology.persistence.typeHandling.bytebuffer.ByteBufferDataWriter;
import org.terasology.persistence.typeHandling.bytebuffer.ByteBufferPersistedSerializer;
import org.terasology.persistence.typeHandling.inMemory.InMemoryPersistedDataSerializer;
import org.terasology.persistence.typeHandling.inMemory.InMemoryReader;
import org.terasology.persistence.typeHandling.inMemory.InMemoryWriter;
import org.terasology.reflection.TypeInfo;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public class FullSerializationDeserializationTest {

    private static final Logger logger = LoggerFactory.getLogger(FullSerializationDeserializationTest.class);

    private static Reflections reflections;
    private static TypeHandlerLibrary typeHandlerLibrary;

    @BeforeAll
    public static void setup() {
        reflections = new Reflections(TypeHandlerLibraryTest.class.getClassLoader());
        typeHandlerLibrary = new TypeHandlerLibrary(reflections);
        TypeHandlerLibrary.populateBuiltInHandlers(typeHandlerLibrary);

    }

    private static Stream<Arguments> values() {
        return Stream.of(
                value(true),
                value((byte) 1),
//                value((short) 1),
                value(1),
                value(1L),
                value(1F),
                value(1D),
                value('c'),
                value("string"),
                value(Locale.ENGLISH),
                value(new SampleClass(
                        "className",
                        -1,
                        new SampleClass2("child"),
                        Lists.newArrayList(
                                new SampleClass2("child1"),
                                new SampleClass2("child2")
                        )
                ))
        );
    }

    private static Stream<Arguments> collections() {
        return Stream.of(
                Arguments.of(new TypeInfo<List<String>>() {
                }, Lists.newArrayList("String1", "String2")),
                Arguments.of(new TypeInfo<Set<String>>() {
                }, Sets.newHashSet("String1", "String2")),
                Arguments.of(new TypeInfo<EnumSet<SampleEnum>>() {
                }, EnumSet.of(SampleEnum.ONE)),
                Arguments.of(new TypeInfo<EnumSet<SampleEnum>>() {
                }, EnumSet.of(SampleEnum.ONE, SampleEnum.THREE)),
                Arguments.of(new TypeInfo<Map<String, String>>() {
                }, new HashMap<String, String>() {{
                    put("key1", "value1");
                }})
//                Arguments.arguments(new TypeInfo<Map<String, List<Map<String, SampleEnum>>>>() {
//                }, new HashMap<String, List<Map<String, SampleEnum>>>() {{
//                    put("key1", Lists.newArrayList(new HashMap() {{
//                        put("innerKey", SampleEnum.TWO);
//                    }}));
//                }}) Runtime delegate type handler fail it
        );
    }

    private static Stream<Arguments> serializers() {
        return Stream.of(
                Arguments.of(
                        new Serializer(typeHandlerLibrary,
                                new ByteBufferPersistedSerializer(),
                                new ByteBufferDataWriter(),
                                new ByteBufferDataReader())),
                Arguments.of(
                        new Serializer(typeHandlerLibrary,
                                new InMemoryPersistedDataSerializer(),
                                new InMemoryWriter(),
                                new InMemoryReader()))
        );
    }

    private static Stream<Arguments> product() {
        return serializers()
                .flatMap(s -> Streams.concat(values(), collections())
                        .map(v -> Arguments.of(s.get()[0], v.get()[0], v.get()[1]))
                );
    }

    private static Arguments value(Object value) {
        return value(value.getClass(), value);
    }

    private static Arguments value(Class clazz, Object value) {
        return Arguments.of(TypeInfo.of(clazz), value);
    }


    @MethodSource("product")
    @ParameterizedTest(name = "{0} : {1} : {2}")
    <T> void test(Serializer<PersistedData> serializer, TypeInfo<T> type, T value) {
        Optional<byte[]> serialized = serializer.serialize(value, type);
        Assertions.assertTrue(serialized.isPresent(), String.format("Serializer didn't serialize type %s", type));
        byte[] bytes = serialized.get();
        logger.info("Size in bytes: {}", bytes.length);
        Optional<T> deserialized = serializer.deserialize(type, bytes);
        Assertions.assertTrue(deserialized.isPresent(), String.format("Serializer didn't deserialize type %s", type));
        Assertions.assertEquals(value, deserialized.get());

    }

    @MethodSource("serializers")
    @ParameterizedTest(name = "{0} : {displayName}")
    void testBoolArray(Serializer<PersistedData> serializer) {
        boolean[] value = new boolean[]{true};
        TypeInfo<boolean[]> type = TypeInfo.of(boolean[].class);
        Optional<byte[]> serialized = serializer.serialize(value, type);
        Assertions.assertTrue(serialized.isPresent(), String.format("Serializer didn't serialize type %s", type));
        byte[] bytes = serialized.get();
        logger.info("Size in bytes: {}", bytes.length);
        Optional<boolean[]> deserialized = serializer.deserialize(type, bytes);
        Assertions.assertTrue(deserialized.isPresent(), String.format("Serializer didn't deserialize type %s", type));
        Assertions.assertArrayEquals(value, deserialized.get());
    }

    @MethodSource("serializers")
    @ParameterizedTest(name = "{0} : {displayName}")
    void testByteArray(Serializer<PersistedData> serializer) {
        byte[] value = new byte[]{(byte) 1};
        TypeInfo<byte[]> type = TypeInfo.of(byte[].class);
        Optional<byte[]> serialized = serializer.serialize(value, type);
        Assertions.assertTrue(serialized.isPresent(), String.format("Serializer didn't serialize type %s", type));
        byte[] bytes = serialized.get();
        logger.info("Size in bytes: {}", bytes.length);
        Optional<byte[]> deserialized = serializer.deserialize(type, bytes);
        Assertions.assertTrue(deserialized.isPresent(), String.format("Serializer didn't deserialize type %s", type));
        Assertions.assertArrayEquals(value, deserialized.get());
    }

    @MethodSource("serializers")
    @ParameterizedTest(name = "{0} : {displayName}")
    void testIntArray(Serializer<PersistedData> serializer) {
        int[] value = new int[]{1};
        TypeInfo<int[]> type = TypeInfo.of(int[].class);
        Optional<byte[]> serialized = serializer.serialize(value, type);
        Assertions.assertTrue(serialized.isPresent(), String.format("Serializer didn't serialize type %s", type));
        byte[] bytes = serialized.get();
        logger.info("Size in bytes: {}", bytes.length);
        Optional<int[]> deserialized = serializer.deserialize(type, bytes);
        Assertions.assertTrue(deserialized.isPresent(), String.format("Serializer didn't deserialize type %s", type));
        Assertions.assertArrayEquals(value, deserialized.get());
    }

    @MethodSource("serializers")
    @ParameterizedTest(name = "{0} : {displayName}")
    void testLongArray(Serializer<PersistedData> serializer) {
        long[] value = new long[]{1L};
        TypeInfo<long[]> type = TypeInfo.of(long[].class);
        Optional<byte[]> serialized = serializer.serialize(value, type);
        Assertions.assertTrue(serialized.isPresent(), String.format("Serializer didn't serialize type %s", type));
        byte[] bytes = serialized.get();
        logger.info("Size in bytes: {}", bytes.length);
        Optional<long[]> deserialized = serializer.deserialize(type, bytes);
        Assertions.assertTrue(deserialized.isPresent(), String.format("Serializer didn't deserialize type %s", type));
        Assertions.assertArrayEquals(value, deserialized.get());
    }

    @MethodSource("serializers")
    @ParameterizedTest(name = "{0} : {displayName}")
    void testFloatArray(Serializer<PersistedData> serializer) {
        float[] value = new float[]{1F};
        TypeInfo<float[]> type = TypeInfo.of(float[].class);
        Optional<byte[]> serialized = serializer.serialize(value, type);
        Assertions.assertTrue(serialized.isPresent(), String.format("Serializer didn't serialize type %s", type));
        byte[] bytes = serialized.get();
        logger.info("Size in bytes: {}", bytes.length);
        Optional<float[]> deserialized = serializer.deserialize(type, bytes);
        Assertions.assertTrue(deserialized.isPresent(), String.format("Serializer didn't deserialize type %s", type));
        Assertions.assertArrayEquals(value, deserialized.get());
    }

    @MethodSource("serializers")
    @ParameterizedTest(name = "{0} : {displayName}")
    void testDoubleArray(Serializer<PersistedData> serializer) {
        double[] value = new double[]{1D};
        TypeInfo<double[]> type = TypeInfo.of(double[].class);
        Optional<byte[]> serialized = serializer.serialize(value, type);
        Assertions.assertTrue(serialized.isPresent(), String.format("Serializer didn't serialize type %s", type));
        byte[] bytes = serialized.get();
        logger.info("Size in bytes: {}", bytes.length);
        Optional<double[]> deserialized = serializer.deserialize(type, bytes);
        Assertions.assertTrue(deserialized.isPresent(), String.format("Serializer didn't deserialize type %s", type));
        Assertions.assertArrayEquals(value, deserialized.get());
    }

    @MethodSource("serializers")
    @ParameterizedTest(name = "{0} : {displayName}")
    void testCharArray(Serializer<PersistedData> serializer) {
        char[] value = new char[]{'c'};
        TypeInfo<char[]> type = TypeInfo.of(char[].class);
        Optional<byte[]> serialized = serializer.serialize(value, type);
        Assertions.assertTrue(serialized.isPresent(), String.format("Serializer didn't serialize type %s", type));
        byte[] bytes = serialized.get();
        logger.info("Size in bytes: {}", bytes.length);
        Optional<char[]> deserialized = serializer.deserialize(type, bytes);
        Assertions.assertTrue(deserialized.isPresent(), String.format("Serializer didn't deserialize type %s", type));
        Assertions.assertArrayEquals(value, deserialized.get());
    }

    @MethodSource("serializers")
    @ParameterizedTest(name = "{0} : {displayName}")
    void testObjectArray(Serializer<PersistedData> serializer) {
        String[] value = new String[]{"String"};
        TypeInfo<String[]> type = TypeInfo.of(String[].class);
        Optional<byte[]> serialized = serializer.serialize(value, type);
        Assertions.assertTrue(serialized.isPresent(), String.format("Serializer didn't serialize type %s", type));
        byte[] bytes = serialized.get();
        logger.info("Size in bytes: {}", bytes.length);
        Optional<String[]> deserialized = serializer.deserialize(type, bytes);
        Assertions.assertTrue(deserialized.isPresent(), String.format("Serializer didn't deserialize type %s", type));
        Assertions.assertArrayEquals(value, deserialized.get());
    }

    public enum SampleEnum {
        ONE,
        TWO,
        THREE
    }

    public static class SampleClass {
        String name;
        int value1;
        SampleClass2 child;
        List<SampleClass2> children;

        public SampleClass(String name, int value1, SampleClass2 child, List<SampleClass2> children) {
            this.name = name;
            this.value1 = value1;
            this.child = child;
            this.children = children;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            SampleClass that = (SampleClass) o;
            return value1 == that.value1
                    && Objects.equals(name, that.name)
                    && Objects.equals(child, that.child)
                    && Objects.equals(children, that.children);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, value1, child, children);
        }
    }

    public static class SampleClass2 {
        String name;

        public SampleClass2(String name) {
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            SampleClass2 that = (SampleClass2) o;
            return Objects.equals(name, that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }
    }
}


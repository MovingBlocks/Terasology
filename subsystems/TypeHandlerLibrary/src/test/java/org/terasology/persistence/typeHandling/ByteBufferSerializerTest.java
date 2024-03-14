// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.persistence.typeHandling;

import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TFloatArrayList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.list.array.TLongArrayList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.internal.util.collections.Sets;
import org.terasology.persistence.typeHandling.bytebuffer.ByteBufferPersistedData;
import org.terasology.persistence.typeHandling.bytebuffer.ByteBufferPersistedDataArray;
import org.terasology.persistence.typeHandling.bytebuffer.ByteBufferPersistedSerializer;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;


class ByteBufferSerializerTest {
    private static PersistedDataSerializer serializer = new ByteBufferPersistedSerializer();

    public static Stream<Arguments> types() {
        return Stream.of(
                Arguments.of(serializer.serialize(1),
                        Sets.newSet(TypeGetter.INTEGER, TypeGetter.LONG, TypeGetter.FLOAT, TypeGetter.DOUBLE)),
                Arguments.of(serializer.serialize(1L),
                        Sets.newSet(TypeGetter.INTEGER, TypeGetter.LONG, TypeGetter.FLOAT, TypeGetter.DOUBLE)),
                Arguments.of(serializer.serialize(1F),
                        Sets.newSet(TypeGetter.INTEGER, TypeGetter.LONG, TypeGetter.FLOAT, TypeGetter.DOUBLE)),
                Arguments.of(serializer.serialize(1D),
                        Sets.newSet(TypeGetter.INTEGER, TypeGetter.LONG, TypeGetter.FLOAT, TypeGetter.DOUBLE)),

                Arguments.of(serializer.serialize("foo"), // TODO
                        Sets.newSet(TypeGetter.STRING)),

                Arguments.of(serializer.serialize(new byte[]{(byte) 0xFF}),
                        Sets.newSet(TypeGetter.BYTE_BUFFER, TypeGetter.BYTES)),
                Arguments.of(serializer.serialize(ByteBuffer.wrap(new byte[]{(byte) 0xFF})),
                        Sets.newSet(TypeGetter.BYTES, TypeGetter.BYTE_BUFFER)),
                Arguments.of(serializer.serialize(true),
                        Sets.newSet(TypeGetter.BOOLEAN))
        );
    }

    @Test
    void serializeString() {
        PersistedData data = serializer.serialize("foo");

        Assertions.assertEquals(ByteBufferPersistedData.class, data.getClass());
        Assertions.assertTrue(data.isString());
        Assertions.assertEquals("foo", data.getAsString());

        Assertions.assertFalse(data.isArray());
        Assertions.assertFalse(data.isNull());
        Assertions.assertFalse(data.isNumber());
        Assertions.assertFalse(data.isBoolean());
        Assertions.assertFalse(data.isBytes());
        Assertions.assertFalse(data.isValueMap());

        Assertions.assertThrows(IllegalStateException.class, data::getAsValueMap);
        Assertions.assertThrows(IllegalStateException.class, data::getAsArray);

        Assertions.assertThrows(DeserializationException.class, data::getAsByteBuffer);
        Assertions.assertThrows(DeserializationException.class, data::getAsBytes);

        Assertions.assertThrows(ClassCastException.class, data::getAsBoolean);
        Assertions.assertThrows(ClassCastException.class, data::getAsInteger);
        Assertions.assertThrows(ClassCastException.class, data::getAsLong);
        Assertions.assertThrows(ClassCastException.class, data::getAsFloat);
        Assertions.assertThrows(ClassCastException.class, data::getAsDouble);
    }

    @Test
    void serializeStrings() {
        PersistedData data = serializer.serialize("foo", "bar");
        Assertions.assertEquals(ByteBufferPersistedDataArray.class, data.getClass());

        Assertions.assertTrue(data.isArray());
        Assertions.assertEquals("foo", data.getAsArray().getArrayItem(0).getAsString());

        Assertions.assertTrue(data.getAsArray().isStringArray());
        Assertions.assertEquals("foo", data.getAsArray().getAsStringArray().get(0));

        Assertions.assertTrue(data.isArray());
        Assertions.assertFalse(data.isString());
        Assertions.assertFalse(data.isNull());
        Assertions.assertFalse(data.isBoolean());
        Assertions.assertFalse(data.isBytes());
        Assertions.assertFalse(data.isValueMap());

        Assertions.assertThrows(IllegalStateException.class, data::getAsString);
        Assertions.assertThrows(IllegalStateException.class, data::getAsValueMap);

        Assertions.assertThrows(DeserializationException.class, data::getAsByteBuffer);
        Assertions.assertThrows(DeserializationException.class, data::getAsBytes);

        Assertions.assertThrows(ClassCastException.class, data::getAsBoolean);
        Assertions.assertThrows(ClassCastException.class, data::getAsInteger);
        Assertions.assertThrows(ClassCastException.class, data::getAsLong);
        Assertions.assertThrows(ClassCastException.class, data::getAsFloat);
        Assertions.assertThrows(ClassCastException.class, data::getAsDouble);
    }

    //TODO remove it
    public void template(PersistedData data) {
        Assertions.assertFalse(data.isString());
        Assertions.assertFalse(data.isArray());
        Assertions.assertFalse(data.isNull());
        Assertions.assertFalse(data.isNumber());
        Assertions.assertFalse(data.isBoolean());
        Assertions.assertFalse(data.isBytes());
        Assertions.assertFalse(data.isValueMap());

        Assertions.assertThrows(IllegalStateException.class, data::getAsValueMap);
        Assertions.assertThrows(IllegalStateException.class, data::getAsArray);

        Assertions.assertThrows(DeserializationException.class, data::getAsByteBuffer);
        Assertions.assertThrows(DeserializationException.class, data::getAsBytes);

        Assertions.assertThrows(ClassCastException.class, data::getAsString);
        Assertions.assertThrows(ClassCastException.class, data::getAsBoolean);
        Assertions.assertThrows(ClassCastException.class, data::getAsInteger);
        Assertions.assertThrows(ClassCastException.class, data::getAsLong);
        Assertions.assertThrows(ClassCastException.class, data::getAsFloat);
        Assertions.assertThrows(ClassCastException.class, data::getAsDouble);
    }

    @Test
    void serializeOneAsStrings() {
        PersistedData data = serializer.serialize(new String[]{"foo"});
        Assertions.assertEquals(ByteBufferPersistedData.class, data.getClass());

        Assertions.assertEquals("foo", data.getAsString());
    }

    @Test
    void serializeStringsIterable() {
        PersistedData data = serializer.serializeStrings(Arrays.asList("foo", "bar"));
        Assertions.assertEquals(ByteBufferPersistedDataArray.class, data.getClass());

        Assertions.assertTrue(data.isArray());
        Assertions.assertEquals("foo", data.getAsArray().getArrayItem(0).getAsString());

        Assertions.assertTrue(data.getAsArray().isStringArray());
        Assertions.assertEquals("foo", data.getAsArray().getAsStringArray().get(0));
    }

    @Test
    void serializeOneAsStringsIterable() {
        PersistedData data = serializer.serializeStrings(Collections.singleton("foo"));
        Assertions.assertEquals(ByteBufferPersistedData.class, data.getClass());

        Assertions.assertEquals("foo", data.getAsString());
    }

    @Test
    void serializeFloat() {
        PersistedData data = serializer.serialize(1f);
        Assertions.assertEquals(ByteBufferPersistedData.class, data.getClass());
        checkIsNumber(data);
    }

    @Test
    void serializeFloats() {
        PersistedData data = serializer.serialize(new float[]{1F});
        checkNumberArray(data, ByteBufferPersistedDataArray.class, ByteBufferPersistedData.class);
    }

    @Test
    void serializeTFloatIterator() {
        PersistedData data = serializer.serialize(TFloatArrayList.wrap(new float[]{1F}).iterator());
        checkNumberArray(data, ByteBufferPersistedDataArray.class, ByteBufferPersistedData.class);
    }

    @Test
    void serializeInt() {
        PersistedData data = serializer.serialize(1);
        Assertions.assertEquals(ByteBufferPersistedData.class, data.getClass());

        checkIsNumber(data);
    }

    @Test
    void serializeInts() {
        PersistedData data = serializer.serialize(new int[]{1});
        checkNumberArray(data, ByteBufferPersistedDataArray.class, ByteBufferPersistedData.class);
    }

    @Test
    void serializeTIntIterator() {
        PersistedData data = serializer.serialize(TIntArrayList.wrap(new int[]{1}).iterator());
        checkNumberArray(data, ByteBufferPersistedDataArray.class, ByteBufferPersistedData.class);
    }

    @Test
    void serializeLong() {
        PersistedData data = serializer.serialize(1L);

        Assertions.assertEquals(ByteBufferPersistedData.class, data.getClass());

        checkIsNumber(data);
    }

    @Test
    void serializeLongs() {
        PersistedData data = serializer.serialize(new long[]{1L});
        checkNumberArray(data, ByteBufferPersistedDataArray.class, ByteBufferPersistedData.class);
    }

    @Test
    void serializeTLongIterator() {
        PersistedData data = serializer.serialize(TLongArrayList.wrap(new long[]{1L}).iterator());
        checkNumberArray(data, ByteBufferPersistedDataArray.class, ByteBufferPersistedData.class);
    }

    @Test
    void serializeBoolean() {
        boolean value = true;
        PersistedData data = serializer.serialize(value);

        Assertions.assertEquals(ByteBufferPersistedData.class, data.getClass());

        Assertions.assertTrue(data.isBoolean());
        Assertions.assertEquals(value, data.getAsBoolean());

        Assertions.assertFalse(data.isString());
        Assertions.assertFalse(data.isArray());
        Assertions.assertFalse(data.isNull());
        Assertions.assertFalse(data.isNumber());
        Assertions.assertFalse(data.isBytes());
        Assertions.assertFalse(data.isValueMap());

        Assertions.assertThrows(IllegalStateException.class, data::getAsValueMap);
        Assertions.assertThrows(IllegalStateException.class, data::getAsArray);

        Assertions.assertThrows(DeserializationException.class, data::getAsByteBuffer);
        Assertions.assertThrows(DeserializationException.class, data::getAsBytes);

        Assertions.assertThrows(ClassCastException.class, data::getAsString);
        Assertions.assertThrows(ClassCastException.class, data::getAsInteger);
        Assertions.assertThrows(ClassCastException.class, data::getAsLong);
        Assertions.assertThrows(ClassCastException.class, data::getAsFloat);
        Assertions.assertThrows(ClassCastException.class, data::getAsDouble);
    }

    @Test
    void serializeBooleans() {
        PersistedData data = serializer.serialize(new boolean[]{true});

        Assertions.assertEquals(ByteBufferPersistedDataArray.class, data.getClass());

        Assertions.assertTrue(data.isArray());

        Assertions.assertFalse(data.isString());
        Assertions.assertFalse(data.isNull());
        Assertions.assertFalse(data.isBoolean());
        Assertions.assertFalse(data.isBytes());
        Assertions.assertFalse(data.isValueMap());

        Assertions.assertThrows(ClassCastException.class, data::getAsString);
        Assertions.assertThrows(IllegalStateException.class, data::getAsValueMap);

        Assertions.assertThrows(DeserializationException.class, data::getAsByteBuffer);
        Assertions.assertThrows(DeserializationException.class, data::getAsBytes);

        Assertions.assertTrue(data.getAsArray().isBooleanArray());

        Assertions.assertTrue(data.getAsBoolean());
        Assertions.assertArrayEquals(new boolean[]{true}, data.getAsArray().getAsBooleanArray());


        Assertions.assertEquals(ByteBufferPersistedData.class, data.getAsArray().getArrayItem(0).getClass());
    }


    @Test
    void serializeBooleansComplicated() {
        boolean[] value = {true, true, true, false, true, false, true, false, true, true, true, false, true};
        PersistedData data = serializer.serialize(value);

        Assertions.assertEquals(ByteBufferPersistedDataArray.class, data.getClass());

        Assertions.assertTrue(data.isArray());

        Assertions.assertFalse(data.isString());
        Assertions.assertFalse(data.isNull());
        Assertions.assertFalse(data.isBoolean());
        Assertions.assertFalse(data.isBytes());
        Assertions.assertFalse(data.isValueMap());

        Assertions.assertThrows(ClassCastException.class, data::getAsString);
        Assertions.assertThrows(IllegalStateException.class, data::getAsValueMap);

        Assertions.assertThrows(DeserializationException.class, data::getAsByteBuffer);
        Assertions.assertThrows(DeserializationException.class, data::getAsBytes);

        Assertions.assertThrows(IllegalStateException.class, data::getAsBoolean);

        Assertions.assertArrayEquals(value, data.getAsArray().getAsBooleanArray());

        Assertions.assertTrue(data.getAsArray().isBooleanArray());


        Assertions.assertEquals(ByteBufferPersistedData.class, data.getAsArray().getArrayItem(0).getClass());
    }

    @Test
    void serializeDouble() {
        PersistedData data = serializer.serialize(1D);
        Assertions.assertEquals(ByteBufferPersistedData.class, data.getClass());

        checkIsNumber(data);
    }

    @Test
    void serializeDoubles() {
        PersistedData data = serializer.serialize(new double[]{1D});
        checkNumberArray(data, ByteBufferPersistedDataArray.class, ByteBufferPersistedData.class);
    }

    @Test
    void serializeTDoubleIterator() {
        PersistedData data = serializer.serialize(TDoubleArrayList.wrap(new double[]{1D}).iterator());
        checkNumberArray(data, ByteBufferPersistedDataArray.class, ByteBufferPersistedData.class);
    }

    @Test
    void serializeBytes() {
        byte[] value = {(byte) 0xFF};
        PersistedData data = serializer.serialize(value);

        Assertions.assertEquals(ByteBufferPersistedData.class, data.getClass());
        Assertions.assertTrue(data.isBytes());
        Assertions.assertArrayEquals(value, data.getAsBytes());
        Assertions.assertEquals(ByteBuffer.wrap(value), data.getAsByteBuffer());

        Assertions.assertFalse(data.isString());
        Assertions.assertFalse(data.isArray());
        Assertions.assertFalse(data.isNull());
        Assertions.assertFalse(data.isNumber());
        Assertions.assertFalse(data.isBoolean());
        Assertions.assertFalse(data.isValueMap());

        Assertions.assertThrows(IllegalStateException.class, data::getAsValueMap);
        Assertions.assertThrows(IllegalStateException.class, data::getAsArray);

        Assertions.assertThrows(ClassCastException.class, data::getAsString);
        Assertions.assertThrows(ClassCastException.class, data::getAsBoolean);
        Assertions.assertThrows(ClassCastException.class, data::getAsInteger);
        Assertions.assertThrows(ClassCastException.class, data::getAsLong);
        Assertions.assertThrows(ClassCastException.class, data::getAsFloat);
        Assertions.assertThrows(ClassCastException.class, data::getAsDouble);
    }

    @Test
    void serializeByteBuffer() {
        byte[] value = {(byte) 0xFF};
        PersistedData data = serializer.serialize(ByteBuffer.wrap(value));

        Assertions.assertEquals(ByteBufferPersistedData.class, data.getClass());
        Assertions.assertTrue(data.isBytes());

        Assertions.assertArrayEquals(value, data.getAsBytes());
        Assertions.assertEquals(ByteBuffer.wrap(value), data.getAsByteBuffer());

        Assertions.assertFalse(data.isString());
        Assertions.assertFalse(data.isArray());
        Assertions.assertFalse(data.isNull());
        Assertions.assertFalse(data.isNumber());
        Assertions.assertFalse(data.isBoolean());
        Assertions.assertFalse(data.isValueMap());

        Assertions.assertThrows(IllegalStateException.class, data::getAsValueMap);
        Assertions.assertThrows(IllegalStateException.class, data::getAsArray);

        Assertions.assertThrows(ClassCastException.class, data::getAsString);
        Assertions.assertThrows(ClassCastException.class, data::getAsBoolean);
        Assertions.assertThrows(ClassCastException.class, data::getAsInteger);
        Assertions.assertThrows(ClassCastException.class, data::getAsLong);
        Assertions.assertThrows(ClassCastException.class, data::getAsFloat);
        Assertions.assertThrows(ClassCastException.class, data::getAsDouble);
    }

    @ParameterizedTest(name = "{1}")
    @MethodSource("types")
    void serializePersistedDatas(PersistedData entry, Set<TypeGetter> typeGetters) {
        PersistedData data = serializer.serialize(entry);
        checkValueArray(data, entry, typeGetters);
    }

    @ParameterizedTest(name = "{1}")
    @MethodSource("types")
    void serializeIterablePersistedData(PersistedData entry, Set<TypeGetter> typeGetters) {
        PersistedData data = serializer.serialize(Collections.singletonList(entry));
        checkValueArray(data, entry, typeGetters);
    }

    @Test
    void serializeMapStringPersistedData() {

    }

    @Test
    void serializeNull() {
        PersistedData data = serializer.serializeNull();

        Assertions.assertTrue(data.isNull());

        Assertions.assertFalse(data.isString());
        Assertions.assertFalse(data.isArray());
        Assertions.assertFalse(data.isNumber());
        Assertions.assertFalse(data.isBoolean());
        Assertions.assertFalse(data.isBytes());
        Assertions.assertFalse(data.isValueMap());

        Assertions.assertThrows(IllegalStateException.class, data::getAsValueMap);
        Assertions.assertThrows(IllegalStateException.class, data::getAsArray);

        Assertions.assertThrows(DeserializationException.class, data::getAsByteBuffer);
        Assertions.assertThrows(DeserializationException.class, data::getAsBytes);

        Assertions.assertThrows(ClassCastException.class, data::getAsString);
        Assertions.assertThrows(ClassCastException.class, data::getAsBoolean);
        Assertions.assertThrows(ClassCastException.class, data::getAsInteger);
        Assertions.assertThrows(ClassCastException.class, data::getAsLong);
        Assertions.assertThrows(ClassCastException.class, data::getAsFloat);
        Assertions.assertThrows(ClassCastException.class, data::getAsDouble);
    }

    private void checkNumberArray(PersistedData data, Class<?> arrayType,
                                  Class<?> entryType) {
        Assertions.assertEquals(arrayType, data.getClass());

        Assertions.assertTrue(data.isArray());

        Assertions.assertFalse(data.isString());
        Assertions.assertFalse(data.isNull());
        Assertions.assertFalse(data.isBoolean());
        Assertions.assertFalse(data.isBytes());
        Assertions.assertFalse(data.isValueMap());

        Assertions.assertThrows(ClassCastException.class, data::getAsString);
        Assertions.assertThrows(IllegalStateException.class, data::getAsValueMap);

        Assertions.assertThrows(DeserializationException.class, data::getAsByteBuffer);
        Assertions.assertThrows(DeserializationException.class, data::getAsBytes);

        Assertions.assertThrows(ClassCastException.class, data::getAsBoolean);

        Assertions.assertEquals(1, data.getAsInteger());
        Assertions.assertEquals(1L, data.getAsLong());
        Assertions.assertEquals(1F, data.getAsFloat());
        Assertions.assertEquals(1D, data.getAsDouble());

        Assertions.assertEquals(entryType, data.getAsArray().getArrayItem(0).getClass());

        Assertions.assertEquals(1, data.getAsArray().getAsIntegerArray().get(0));
        Assertions.assertEquals(1L, data.getAsArray().getAsLongArray().get(0));
        Assertions.assertEquals(1F, data.getAsArray().getAsFloatArray().get(0));
        Assertions.assertEquals(1D, data.getAsArray().getAsDoubleArray().get(0));
    }

    private void checkIsNumber(PersistedData data) {
        Assertions.assertTrue(data.isNumber());

        Assertions.assertEquals(1, data.getAsInteger());
        Assertions.assertEquals(1L, data.getAsLong());
        Assertions.assertEquals(1F, data.getAsFloat());
        Assertions.assertEquals(1D, data.getAsDouble());

        Assertions.assertFalse(data.isString());
        Assertions.assertFalse(data.isArray());
        Assertions.assertFalse(data.isNull());
        Assertions.assertFalse(data.isBoolean());
        Assertions.assertFalse(data.isBytes());
        Assertions.assertFalse(data.isValueMap());

        Assertions.assertThrows(IllegalStateException.class, data::getAsValueMap);
        Assertions.assertThrows(IllegalStateException.class, data::getAsArray);

        Assertions.assertThrows(DeserializationException.class, data::getAsByteBuffer);
        Assertions.assertThrows(DeserializationException.class, data::getAsBytes);

        Assertions.assertThrows(ClassCastException.class, data::getAsString);
        Assertions.assertThrows(ClassCastException.class, data::getAsBoolean);
    }

    private void checkValueArray(PersistedData data, PersistedData entry, Set<TypeGetter> typeGetters) {
        Assertions.assertEquals(ByteBufferPersistedDataArray.class, data.getClass());

//        Assertions.assertEquals(entry, data.getAsArray().getArrayItem(0));
        typeGetters.forEach(typeGetter -> {
                    Object expected = typeGetter.getGetter().apply(entry);
                    Object actual = typeGetter.getGetter().apply(data);
                    if (typeGetter == TypeGetter.BYTES) {
                        Assertions.assertArrayEquals((byte[]) expected, (byte[]) actual);
                    } else {
                        Assertions.assertEquals(expected, actual);
                    }
                }
        );


        Assertions.assertFalse(data.isString());
        Assertions.assertFalse(data.isNull());
        Assertions.assertFalse(data.isNumber());
        Assertions.assertFalse(data.isBoolean());
        Assertions.assertFalse(data.isBytes());
        Assertions.assertFalse(data.isValueMap());

        Assertions.assertThrows(IllegalStateException.class, data::getAsValueMap);

        Set<TypeGetter> deserializationExceptionGetters = Sets.newSet(
                TypeGetter.BYTE_BUFFER,
                TypeGetter.BYTES
        );
        deserializationExceptionGetters.stream()
                .filter(f -> !typeGetters.contains(f))
                .map(TypeGetter::getGetter)
                .map(f -> (Executable) () -> f.apply(data))
                .forEach(e ->
                        Assertions.assertThrows(DeserializationException.class, e)
                );

        Set<TypeGetter> classCastExceptionGetters = Sets.newSet(
                TypeGetter.BOOLEAN,
                TypeGetter.STRING,
                TypeGetter.INTEGER,
                TypeGetter.LONG,
                TypeGetter.FLOAT,
                TypeGetter.DOUBLE
        );
        classCastExceptionGetters.stream().filter(f -> !typeGetters.contains(f))
                .map(TypeGetter::getGetter)
                .map(f -> (Executable) () -> f.apply(data))
                .forEach(e ->
                        Assertions.assertThrows(ClassCastException.class, e)
                );
    }

    private enum TypeGetter {
        STRING(PersistedData::getAsString),
        BOOLEAN(PersistedData::getAsBoolean),
        INTEGER(PersistedData::getAsInteger),
        LONG(PersistedData::getAsLong),
        FLOAT(PersistedData::getAsFloat),
        DOUBLE(PersistedData::getAsDouble),
        BYTE_BUFFER(PersistedData::getAsByteBuffer),
        BYTES(PersistedData::getAsBytes);

        private final Function<PersistedData, Object> getter;

        TypeGetter(Function<PersistedData, Object> getter) {
            this.getter = getter;
        }

        public Function<PersistedData, Object> getGetter() {
            return getter;
        }
    }
}

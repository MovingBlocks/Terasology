// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.persistence.typeHandling.coreTypes;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.TypeHandler;
import org.terasology.persistence.typeHandling.inMemory.InMemoryPersistedDataSerializer;
import org.terasology.persistence.typeHandling.inMemory.PersistedBoolean;
import org.terasology.persistence.typeHandling.inMemory.PersistedDouble;
import org.terasology.persistence.typeHandling.inMemory.PersistedFloat;
import org.terasology.persistence.typeHandling.inMemory.PersistedInteger;
import org.terasology.persistence.typeHandling.inMemory.PersistedLong;
import org.terasology.persistence.typeHandling.inMemory.PersistedString;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.stream.Stream;

class SimpleCoreHandlerTest {

    private static Stream<Arguments> primitives() {
        return Stream.of(
                Arguments.of(true, new BooleanTypeHandler(), new PersistedBoolean(true)),

                Arguments.of(1.0, new NumberTypeHandler(), new PersistedDouble(1)),
                Arguments.of(1, new IntTypeHandler(), new PersistedInteger(1)),
                Arguments.of(1L, new LongTypeHandler(), new PersistedLong(1)),
                Arguments.of(1.0F, new FloatTypeHandler(), new PersistedFloat(1)),
                Arguments.of(1.0, new DoubleTypeHandler(), new PersistedDouble(1)),

                Arguments.of("foo", new StringTypeHandler(), new PersistedString("foo")),
                Arguments.of('f', new CharacterTypeHandler(), new PersistedString("f")));
    }

    @ParameterizedTest(name = "{1}")
    @DisplayName("Check simple core types serialization")
    @MethodSource("primitives")
    <T> void serialization(T obj, TypeHandler<T> typeHandler, PersistedData expectedData) throws Exception {
        PersistedData data = typeHandler.serialize(obj, new InMemoryPersistedDataSerializer());

        Assertions.assertEquals(expectedData.getClass(), data.getClass());
        Assertions.assertEquals(getData(expectedData), getData(data));
    }

    @ParameterizedTest(name = "{1}")
    @DisplayName("Chech simple core types deserialization")
    @MethodSource("primitives")
    <T> void deserialization(T expectedObj, TypeHandler<T> typeHandler, PersistedData data) throws Exception {
        Optional<T> optionalObj = typeHandler.deserialize(data);
        Assertions.assertTrue(optionalObj.isPresent());
        T obj = optionalObj.get();
        Assertions.assertEquals(expectedObj.getClass(), obj.getClass());
        Assertions.assertEquals(expectedObj, obj);
    }

    private Object getData(PersistedData persistedData) throws Exception {
        //I hope that first field - is needs field.
        Field dataField = persistedData.getClass().getDeclaredFields()[0];
        dataField.setAccessible(true);
        return dataField.get(persistedData);
    }
}

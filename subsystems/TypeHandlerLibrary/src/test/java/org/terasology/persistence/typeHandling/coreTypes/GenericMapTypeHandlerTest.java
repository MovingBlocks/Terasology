// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.persistence.typeHandling.coreTypes;

import org.junit.jupiter.api.Test;
import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.PersistedDataSerializer;
import org.terasology.persistence.typeHandling.TypeHandler;
import org.terasology.persistence.typeHandling.inMemory.PersistedLong;
import org.terasology.persistence.typeHandling.inMemory.PersistedMap;
import org.terasology.persistence.typeHandling.inMemory.PersistedString;
import org.terasology.persistence.typeHandling.inMemory.arrays.PersistedValueArray;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth8.assertThat;

class GenericMapTypeHandlerTest {

    private static final String TEST_KEY = "health:baseRegen";
    private static final long TEST_VALUE = -1;

    private final PersistedData testData = new PersistedValueArray(List.of(
            new PersistedMap(Map.of(
                    GenericMapTypeHandler.KEY, new PersistedString(TEST_KEY),
                    GenericMapTypeHandler.VALUE, new PersistedLong(TEST_VALUE)
            ))
    ));

    private final PersistedData testDataMalformatted = new PersistedValueArray(List.of(
            new PersistedMap(Map.of(
                    "testmap", new PersistedMap(Map.of(
                            TEST_KEY, new PersistedLong(TEST_VALUE)
                    ))
            ))
    ));

    private final PersistedData testDataMissingKeyEntry = new PersistedValueArray(List.of(
            new PersistedMap(Map.of(
                    "not key", new PersistedString(TEST_KEY),
                    GenericMapTypeHandler.VALUE, new PersistedLong(TEST_VALUE)
            ))
    ));

    private final PersistedData testDataMissingValueEntry = new PersistedValueArray(List.of(
            new PersistedMap(Map.of(
                    GenericMapTypeHandler.KEY, new PersistedString(TEST_KEY),
                    "not value", new PersistedLong(TEST_VALUE)
            ))
    ));

    private final PersistedData testDataValidAndInvalidMix = new PersistedValueArray(List.of(
            new PersistedMap(Map.of(
                    GenericMapTypeHandler.KEY, new PersistedString(TEST_KEY),
                    GenericMapTypeHandler.VALUE, new PersistedLong(TEST_VALUE)
            )),
            new PersistedMap(Map.of(
                    "not key", new PersistedString(TEST_KEY),
                    "not value", new PersistedLong(TEST_VALUE)
            ))
    ));

    @Test
    void testDeserialize() {
        var th = new GenericMapTypeHandler<>(
                new StringTypeHandler(),
                new LongTypeHandler()
        );

        assertThat(th.deserialize(testData)).isPresent();
        assertThat(th.deserialize(testData).get()).containsExactly(TEST_KEY, TEST_VALUE);
    }

    @Test
    void testDeserializeWithMismatchedValueHandler() {
        var th = new GenericMapTypeHandler<>(
                new StringTypeHandler(),
                new UselessTypeHandler<>()
        );

        assertThat(th.deserialize(testData)).isEmpty();
    }

    @Test
    void testDeserializeWithMismatchedKeyHandler() {
        var th = new GenericMapTypeHandler<>(
                new UselessTypeHandler<>(),
                new LongTypeHandler()
        );

        assertThat(th.deserialize(testData)).isEmpty();
    }
    
    @Test
    void testDeserializeWithObjectInsteadOfArray() {
        var th = new GenericMapTypeHandler<>(
                new StringTypeHandler(),
                new LongTypeHandler()
        );

        assertThat(th.deserialize(testDataMalformatted)).isEmpty();
    }

    @Test
    void testDeserializeWithMissingKeyEntry() {
        var th = new GenericMapTypeHandler<>(
                new StringTypeHandler(),
                new LongTypeHandler()
        );

        assertThat(th.deserialize(testDataMissingKeyEntry)).isEmpty();
    }

    @Test
    void testDeserializeWithMissingValueEntry() {
        var th = new GenericMapTypeHandler<>(
                new StringTypeHandler(),
                new LongTypeHandler()
        );

        assertThat(th.deserialize(testDataMissingValueEntry)).isEmpty();
    }

    @Test
    void testDeserializeWithValidAndInvalidEntries() {
        var th = new GenericMapTypeHandler<>(
                new StringTypeHandler(),
                new LongTypeHandler()
        );

        assertThat(th.deserialize(testDataValidAndInvalidMix)).isEmpty();
    }

    /** Never returns a value. */
    private static class UselessTypeHandler<T> extends TypeHandler<T> {
        @Override
        protected PersistedData serializeNonNull(Object value, PersistedDataSerializer serializer) {
            return null;
        }

        @Override
        public Optional<T> deserialize(PersistedData data) {
            return Optional.empty();
        }
    }
}

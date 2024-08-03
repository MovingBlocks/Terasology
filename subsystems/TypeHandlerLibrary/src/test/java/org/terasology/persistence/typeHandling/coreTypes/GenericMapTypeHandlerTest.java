// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.persistence.typeHandling.coreTypes;

import org.junit.jupiter.api.DisplayName;
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

    /**
     * JSON equivalent:
     * <pre><code>
     * [
     *   {
     *       "key": "health:baseRegen",
     *       "value": -1
     *   }
     * ]
     * </code></pre>
     */
    private final PersistedData testData = new PersistedValueArray(List.of(
            new PersistedMap(Map.of(
                    GenericMapTypeHandler.KEY, new PersistedString(TEST_KEY),
                    GenericMapTypeHandler.VALUE, new PersistedLong(TEST_VALUE)
            ))
    ));

    /**
     * JSON equivalent:
     * <pre><code>
     * {
     *   "health:baseRegen": -1
     * }
     * </code></pre>
     */
    private final PersistedData testDataMalformatted = new PersistedValueArray(List.of(
            new PersistedMap(Map.of(
                    TEST_KEY, new PersistedLong(TEST_VALUE)
            ))
    ));

    /**
     * JSON equivalent:
     * <pre><code>
     * [
     *   {
     *       "not key": "health:baseRegen",
     *       "value": -1
     *   }
     * ]
     * </code></pre>
     */
    private final PersistedData testDataMissingKeyEntry = new PersistedValueArray(List.of(
            new PersistedMap(Map.of(
                    "not key", new PersistedString(TEST_KEY),
                    GenericMapTypeHandler.VALUE, new PersistedLong(TEST_VALUE)
            ))
    ));

    /**
     * JSON equivalent:
     * <pre><code>
     * [
     *   {
     *       "key": "health:baseRegen",
     *       "not value": -1
     *   }
     * ]
     * </code></pre>
     */
    private final PersistedData testDataMissingValueEntry = new PersistedValueArray(List.of(
            new PersistedMap(Map.of(
                    GenericMapTypeHandler.KEY, new PersistedString(TEST_KEY),
                    "not value", new PersistedLong(TEST_VALUE)
            ))
    ));

    /**
     * JSON equivalent:
     * <pre><code>
     * [
     *   {
     *       "key": "health:baseRegen",
     *       "value": -1
     *   },
     *   {
     *       "not key": "health:baseRegen",
     *       "not value": -1
     *   },
     * ]
     * </code></pre>
     */
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

    /**
     * JSON equivalent:
     * <pre><code>
     * [ ]
     * </code></pre>
     */
    private final PersistedData testDataValidEmpty = new PersistedValueArray(List.of());

    @Test
    @DisplayName("Data with valid formatting can be deserialized successfully.")
    void testDeserialize() {
        var th = new GenericMapTypeHandler<>(
                new StringTypeHandler(),
                new LongTypeHandler()
        );

        assertThat(th.deserialize(testData)).isPresent();
        assertThat(th.deserialize(testData).get()).containsExactly(TEST_KEY, TEST_VALUE);
    }

    @Test
    @DisplayName("Deserializing valid data with a mismatching value type handler fails deserialization (returns empty `Optional`)")
    void testDeserializeWithMismatchedValueHandler() {
        var th = new GenericMapTypeHandler<>(
                new StringTypeHandler(),
                new UselessTypeHandler<>()
        );

        assertThat(th.deserialize(testData)).isEmpty();
    }

    @Test
    @DisplayName("Deserializing valid data with a mismatching key type handler fails deserialization (returns empty `Optional`)")
    void testDeserializeWithMismatchedKeyHandler() {
        var th = new GenericMapTypeHandler<>(
                new UselessTypeHandler<>(),
                new LongTypeHandler()
        );

        assertThat(th.deserialize(testData)).isEmpty();
    }
    
    @Test
    @DisplayName("Incorrectly formatted data (without an outer array) fails deserialization (returns empty `Optional`)")
    void testDeserializeWithObjectInsteadOfArray() {
        var th = new GenericMapTypeHandler<>(
                new StringTypeHandler(),
                new LongTypeHandler()
        );

        assertThat(th.deserialize(testDataMalformatted)).isEmpty();
    }

    @Test
    @DisplayName("Incorrectly formatted data (without a map entry with key \"key\") fails deserialization (returns empty `Optional`)")
    void testDeserializeWithMissingKeyEntry() {
        var th = new GenericMapTypeHandler<>(
                new StringTypeHandler(),
                new LongTypeHandler()
        );

        assertThat(th.deserialize(testDataMissingKeyEntry)).isEmpty();
    }

    @Test
    @DisplayName("Incorrectly formatted data (without a map entry with key \"value\") fails deserialization (returns empty `Optional`)")
    void testDeserializeWithMissingValueEntry() {
        var th = new GenericMapTypeHandler<>(
                new StringTypeHandler(),
                new LongTypeHandler()
        );

        assertThat(th.deserialize(testDataMissingValueEntry)).isEmpty();
    }

    @Test
    @DisplayName("A map containing both, correctly and incorrectly formatted data, fails deserialization (returns empty `Optional`)")
    void testDeserializeWithValidAndInvalidEntries() {
        var th = new GenericMapTypeHandler<>(
                new StringTypeHandler(),
                new LongTypeHandler()
        );

        assertThat(th.deserialize(testDataValidAndInvalidMix)).isEmpty();
    }

    @Test
    @DisplayName("An empty map encoded as empty array '[]' can be deserialized successfully.")
    void testDeserializeEmptyMap() {
        var th = new GenericMapTypeHandler<>(new StringTypeHandler(), new LongTypeHandler());

        var result = th.deserialize(testDataValidEmpty);

        assertThat(result).isPresent();
        assertThat(result.get()).isEmpty();
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

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

import java.util.Collections;
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

        assertThat(th.deserialize(testData)).hasValue(Collections.emptyMap());
    }

    @Test
    void testDeserializeWithMismatchedKeyHandler() {
        var th = new GenericMapTypeHandler<>(
                new UselessTypeHandler<>(),
                new LongTypeHandler()
        );

        assertThat(th.deserialize(testData)).hasValue(Collections.emptyMap());
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

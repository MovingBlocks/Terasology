// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.persistence.serializers.gson;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.junit.jupiter.api.Test;
import org.terasology.persistence.serializers.gson.models.TestColor;
import org.terasology.persistence.serializers.gson.typehandler.TestColorTypeHandler;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GsonTypeHandlerAdapterTest {
    private static final String OBJECT_JSON_ARRAY = "{\"color\":[222,173,190,239],\"i\":-123}";
    private static final String OBJECT_JSON_HEX = "{\"color\":DEADBEEF,\"i\":-123}";
    private static final TestClass OBJECT = new TestClass(new TestColor(0xDEADBEEF), -123);

    private final Gson gson = GsonBuilderFactory.createGsonBuilderWithTypeHandlers(
            TypeHandlerEntry.of(TestColor.class, new TestColorTypeHandler())
    )
            .create();

    /**
     * {@link GsonTypeHandlerAdapter#read(JsonReader)} is tested by deserializing an object from JSON
     * via Gson with a registered {@link GsonTypeHandlerAdapterFactory} which creates instances of
     * {@link GsonTypeHandlerAdapter}.
     */
    @Test
    void testRead() {
        // Deserialize object with color as JSON array
        TestClass deserializedObject = gson.fromJson(OBJECT_JSON_ARRAY, TestClass.class);

        assertEquals(OBJECT, deserializedObject);

        // Deserialize object with color as hex string
        deserializedObject = gson.fromJson(OBJECT_JSON_HEX, TestClass.class);

        assertEquals(OBJECT, deserializedObject);
    }

    /**
     * {@link GsonTypeHandlerAdapter#write(JsonWriter, Object)} is tested by serializing an object to JSON
     * via Gson with a registered {@link GsonTypeHandlerAdapterFactory} which creates instances of
     * {@link GsonTypeHandlerAdapter}.
     */
    @Test
    void testWrite() {
        String serializedObject = gson.toJson(OBJECT);

        assertEquals(OBJECT_JSON_ARRAY, serializedObject);
    }

    private static class TestClass {
        private final TestColor color;
        private final int i;

        private TestClass(TestColor color, int i) {
            this.color = color;
            this.i = i;
        }

        @Override
        public int hashCode() {
            return Objects.hash(color, i);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TestClass testClass = (TestClass) o;
            return i == testClass.i &&
                    Objects.equals(color, testClass.color);
        }
    }
}

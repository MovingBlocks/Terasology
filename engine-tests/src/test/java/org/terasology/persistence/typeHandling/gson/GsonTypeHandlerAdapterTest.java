// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.persistence.typeHandling.gson;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.junit.jupiter.api.Test;
import org.terasology.nui.Color;
import org.terasology.engine.persistence.typeHandling.extensionTypes.ColorTypeHandler;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GsonTypeHandlerAdapterTest {
    private static final String OBJECT_JSON_ARRAY = "{\"color\":[222,173,190,239],\"i\":-123}";
    private static final String OBJECT_JSON_HEX = "{\"color\":DEADBEEF,\"i\":-123}";
    private static final TestClass OBJECT = new TestClass(new Color(0xDEADBEEF), -123);

    private final Gson gson = GsonBuilderFactory.createGsonBuilderWithTypeHandlers(
            TypeHandlerEntry.of(Color.class, new ColorTypeHandler())
    )
            .create();

    /**
     * {@link GsonTypeHandlerAdapter#read(JsonReader)} is tested by deserializing an object from JSON
     * via Gson with a registered {@link GsonTypeHandlerAdapterFactory} which creates instances of
     * {@link GsonTypeHandlerAdapter}.
     */
    @Test
    public void testRead() {
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
    public void testWrite() {
        String serializedObject = gson.toJson(OBJECT);

        assertEquals(OBJECT_JSON_ARRAY, serializedObject);
    }

    private static final class TestClass {
        private final Color color;
        private final int i;

        private TestClass(Color color, int i) {
            this.color = color;
            this.i = i;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            TestClass testClass = (TestClass) o;
            return i == testClass.i
                    && Objects.equals(color, testClass.color);
        }
    }
}

// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.persistence.typeHandling.gson;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import org.joml.Vector4f;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;
import org.terasology.joml.geom.Rectanglei;
import org.terasology.nui.Color;
import org.terasology.persistence.typeHandling.TypeHandlerLibrary;
import org.terasology.persistence.typeHandling.TypeHandlerLibraryImpl;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GsonTypeHandlerLibraryAdapterFactoryTest {
    private static final TestClass OBJECT = new TestClass(
            new Color(0xDEADBEEF),
            ImmutableSet.of(new Vector4f(0,0,0,0), new Vector4f(1,1,1,1)),
            ImmutableMap.of(
                    "someRect",
                    new Rectanglei(-3, -3).setSize(10, 10)
            ),
            ImmutableMap.of(0, 1, 1, 0),
            -0xDECAF
    );

    private static final String OBJECT_JSON = "{\"color\":[222,173,190,239],\"vector4fs\":[[0.0,0.0,0.0,0.0]," +
            "[1.0,1.0,1.0,1.0]],\"rectangleiMap\":{\"someRect\":{\"min\":[-3,-3],\"max\":[7,7]}},\"i\":-912559}";

    private final Reflections reflections = new Reflections(getClass().getClassLoader());

    private final TypeHandlerLibrary typeHandlerLibrary =
            TypeHandlerLibraryImpl.withReflections(reflections);

    private final Gson gson =
            GsonBuilderFactory.createGsonBuilderWithTypeSerializationLibrary(typeHandlerLibrary)
            .create();

    @Test
    public void testSerialize() {
        String serializedObject = gson.toJson(OBJECT);

        assertEquals(OBJECT_JSON, serializedObject);
    }

    @Test
    public void testDeserialize() {
        TestClass deserializedObject = gson.fromJson(OBJECT_JSON, TestClass.class);

        assertEquals(OBJECT, deserializedObject);
    }

    private static class TestClass {
        private final Color color;
        private final Set<Vector4f> vector4fs;
        private final Map<String, Rectanglei> rectangleiMap;

        // Will not be serialized
        private final Map<Integer, Integer> intMap;

        private final int i;

        private TestClass(Color color, Set<Vector4f> vector4fs, Map<String, Rectanglei> rectangleiMap,
                          Map<Integer, Integer> intMap, int i) {
            this.color = color;
            this.vector4fs = vector4fs;
            this.rectangleiMap = rectangleiMap;
            this.intMap = intMap;
            this.i = i;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TestClass testClass = (TestClass) o;
            return i == testClass.i &&
                    Objects.equals(color, testClass.color) &&
                    Objects.equals(vector4fs, testClass.vector4fs) &&
                    Objects.equals(rectangleiMap, testClass.rectangleiMap);
        }
    }
}

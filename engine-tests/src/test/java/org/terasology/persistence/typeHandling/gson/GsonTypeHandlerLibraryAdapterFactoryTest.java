/*
 * Copyright 2017 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.persistence.typeHandling.gson;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector4f;
import org.terasology.nui.Color;
import org.terasology.persistence.typeHandling.TypeHandlerLibrary;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GsonTypeHandlerLibraryAdapterFactoryTest {
    private static final TestClass OBJECT = new TestClass(
            new Color(0xDEADBEEF),
            ImmutableSet.of(Vector4f.zero(), Vector4f.one()),
            ImmutableMap.of(
                    "someRect",
                    Rect2i.createFromMinAndSize(-3, -3, 10, 10)
            ),
            ImmutableMap.of(0, 1, 1, 0),
            -0xDECAF
    );

    private static final String OBJECT_JSON = "{\"color\":[222,173,190,239],\"vector4fs\":[[0.0,0.0,0.0,0.0]," +
            "[1.0,1.0,1.0,1.0]],\"rect2iMap\":{\"someRect\":{\"min\":[-3,-3],\"size\":[10,10]}},\"i\":-912559}";

    private final Reflections reflections = new Reflections(getClass().getClassLoader());

    private final TypeHandlerLibrary typeHandlerLibrary =
            TypeHandlerLibrary.withReflections(reflections);

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
        private final Map<String, Rect2i> rect2iMap;

        // Will not be serialized
        private final Map<Integer, Integer> intMap;

        private final int i;

        private TestClass(Color color, Set<Vector4f> vector4fs, Map<String, Rect2i> rect2iMap,
                          Map<Integer, Integer> intMap, int i) {
            this.color = color;
            this.vector4fs = vector4fs;
            this.rect2iMap = rect2iMap;
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
                    Objects.equals(rect2iMap, testClass.rect2iMap);
        }
    }
}

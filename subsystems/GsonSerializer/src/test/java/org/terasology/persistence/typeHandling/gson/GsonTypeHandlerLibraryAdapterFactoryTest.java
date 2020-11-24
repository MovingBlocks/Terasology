// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.persistence.typeHandling.gson;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;
import org.terasology.persistence.typeHandling.TypeHandlerLibrary;
import org.terasology.persistence.typeHandling.gson.models.TestClass;
import org.terasology.persistence.typeHandling.gson.models.TestColor;
import org.terasology.persistence.typeHandling.gson.models.TestRect2i;
import org.terasology.persistence.typeHandling.gson.models.TestVector4f;
import org.terasology.persistence.typeHandling.gson.typehandler.TestColorTypeHandler;
import org.terasology.persistence.typeHandling.gson.typehandler.TestRect2iTypeHandler;
import org.terasology.persistence.typeHandling.gson.typehandler.TestVector4fTypeHandler;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GsonTypeHandlerLibraryAdapterFactoryTest {

    private static final TestClass OBJECT = new TestClass(
            new TestColor(0xDEADBEEF),
            ImmutableSet.of(new TestVector4f(0, 0, 0, 0), new TestVector4f(1, 1, 1, 1)),
            ImmutableMap.of(
                    "someRect",
                    new TestRect2i(-3, -3, 10, 10)
            ),
            ImmutableMap.of(0, 1, 1, 0),
            -0xDECAF
    );
    private static final String OBJECT_JSON = "{\"color\":[222,173,190,239],\"vector4fs\":[[0.0,0.0,0.0,0.0]," +
            "[1.0,1.0,1.0,1.0]],\"rect2iMap\":{\"someRect\":{\"min\":[-3,-3],\"size\":[10,10]}},\"i\":-912559}";

    private final Reflections reflections = new Reflections(getClass().getClassLoader());
    private final TypeHandlerLibrary typeHandlerLibrary = new TypeHandlerLibrary(reflections);
    private final Gson gson = GsonBuilderFactory.createGsonBuilderWithTypeSerializationLibrary(typeHandlerLibrary)
            .create();

    @BeforeEach
    void setup() {
        typeHandlerLibrary.addTypeHandler(TestColor.class, new TestColorTypeHandler());
        typeHandlerLibrary.addTypeHandler(TestVector4f.class, new TestVector4fTypeHandler());
        typeHandlerLibrary.addTypeHandler(TestRect2i.class, new TestRect2iTypeHandler());
    }

    @Test
    void testSerialize() {
        String serializedObject = gson.toJson(OBJECT);

        assertEquals(OBJECT_JSON, serializedObject);
    }

    @Test
    void testDeserialize() {
        TestClass deserializedObject = gson.fromJson(OBJECT_JSON, TestClass.class);

        assertEquals(OBJECT, deserializedObject);
    }

}

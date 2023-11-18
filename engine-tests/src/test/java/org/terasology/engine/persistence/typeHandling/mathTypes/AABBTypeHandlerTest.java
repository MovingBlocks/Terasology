// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.persistence.typeHandling.mathTypes;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;
import org.terasology.engine.persistence.typeHandling.TypeHandlerLibraryImpl;
import org.terasology.engine.persistence.typeHandling.gson.GsonBuilderFactory;
import org.terasology.joml.geom.AABBf;
import org.terasology.joml.geom.AABBi;
import org.terasology.persistence.typeHandling.TypeHandlerLibrary;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AABBTypeHandlerTest extends MathTypeAssert {
    private final Reflections reflections = new Reflections(getClass().getClassLoader());
    private final TypeHandlerLibrary typeHandlerLibrary = TypeHandlerLibraryImpl.withReflections(reflections);

    private final Gson gson =
        GsonBuilderFactory.createGsonBuilderWithTypeSerializationLibrary(typeHandlerLibrary)
            .create();

    @Test
    public void testSerializeAABBi() {
        AABB1Test aabb1 = new AABB1Test();
        aabb1.a1 = new AABBi(0, 0, 0, 10, 10, 10);

        JsonElement tree = gson.toJsonTree(aabb1);

        JsonObject obj = tree.getAsJsonObject();
        assertTrue(obj.has("a1"));
        assertAABBi(obj.get("a1"), 0, 0, 0, 10, 10, 10);
    }

    @Test
    public void testSerializeAABB1() {
        AABB3Test aabb3 = new AABB3Test();
        aabb3.a1 = new AABBf(10.0f, 5.0f, 0, 5.3f, 2.0f, 2.2f);
        aabb3.a2 = new AABBi(0, 0, 0, 10, 10, 10);

        JsonElement tree = gson.toJsonTree(aabb3);

        JsonObject obj = tree.getAsJsonObject();
        assertTrue(obj.has("a1"));
        assertAABBf(obj.get("a1"), 10.0f, 5.0f, 0, 5.3f, 2.0f, 2.2f);
        assertTrue(obj.has("a2"));
        assertAABBi(obj.get("a2"), 0, 0, 0, 10, 10, 10);
    }


    @Test
    public void testSerializeAABB1Missing() {
        AABB3Test aabb3 = new AABB3Test();
        aabb3.a1 = new AABBf(10.0f, 5.0f, 0, 5.3f, 2.0f, 2.2f);

        JsonElement tree = gson.toJsonTree(aabb3);

        JsonObject obj = tree.getAsJsonObject();
        assertTrue(obj.has("a1"));
        assertAABBf(obj.get("a1"), 10.0f, 5.0f, 0, 5.3f, 2.0f, 2.2f);
        assertFalse(obj.has("a2"));
    }


    @Test
    public void testSerializeAABBf() {
        AABB2Test aabb1 = new AABB2Test();
        aabb1.a1 = new AABBf(0, 2.0f, 1.5f, 10.0f, 5.0f, 10);

        JsonElement tree = gson.toJsonTree(aabb1);

        JsonObject obj = tree.getAsJsonObject();
        assertTrue(obj.has("a1"));
        assertAABBf(obj.get("a1"), 0, 2.0f, 1.5f, 10.0f, 5.0f, 10);
    }

    public static class AABB1Test {
        public AABBi a1;
    }

    public static class AABB2Test {
        public AABBf a1;
    }

    public static class AABB3Test {
        public AABBf a1;
        public AABBi a2;
    }
}

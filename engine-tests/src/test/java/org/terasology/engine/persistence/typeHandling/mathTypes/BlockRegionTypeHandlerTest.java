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
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.joml.geom.AABBi;
import org.terasology.persistence.typeHandling.TypeHandlerLibrary;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class BlockRegionTypeHandlerTest extends MathTypeAssert {
    private final Reflections reflections = new Reflections(getClass().getClassLoader());
    private final TypeHandlerLibrary typeHandlerLibrary = TypeHandlerLibraryImpl.withReflections(reflections);

    private final Gson gson =
            GsonBuilderFactory.createGsonBuilderWithTypeSerializationLibrary(typeHandlerLibrary).create();

    @Test
    public void testSerializeBlockRegion() {
        AABBBlockRegion1Test aabb1 = new AABBBlockRegion1Test();
        aabb1.a1 = new BlockRegion(5, 5, 5, 13, 12, 14);
        aabb1.a2 = new AABBi(3, 5, 5, 22, 12, 14);

        JsonElement tree = gson.toJsonTree(aabb1);

        JsonObject obj = tree.getAsJsonObject();
        assertTrue(obj.has("a1"));
        assertBlockRegion(obj.get("a1"), 5, 5, 5, 13, 12, 14);
        assertTrue(obj.has("a2"));
        assertAABBi(obj.get("a2"), 3, 5, 5, 22, 12, 14);
    }

    public static class AABBBlockRegion1Test {
        public BlockRegion a1;
        public AABBi a2;
    }
}

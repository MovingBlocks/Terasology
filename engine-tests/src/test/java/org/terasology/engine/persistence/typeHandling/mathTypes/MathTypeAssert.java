// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.persistence.typeHandling.mathTypes;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MathTypeAssert {

    public void assertAABBf(JsonElement element, float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        JsonObject m1 = element.getAsJsonObject();
        assertTrue(m1.has("min"));
        assertEquals(m1.get("min").getAsJsonArray().size(), 3);
        assertEquals(m1.get("min").getAsJsonArray().get(0).getAsFloat(), minX, 0.001f);
        assertEquals(m1.get("min").getAsJsonArray().get(1).getAsFloat(), minY, 0.001f);
        assertEquals(m1.get("min").getAsJsonArray().get(2).getAsFloat(), minZ, 0.001f);
        assertTrue(m1.has("max"));
        assertEquals(m1.get("max").getAsJsonArray().size(), 3);
        assertEquals(m1.get("max").getAsJsonArray().get(0).getAsFloat(), maxX, 0.001f);
        assertEquals(m1.get("max").getAsJsonArray().get(1).getAsFloat(), maxY, 0.001f);
        assertEquals(m1.get("max").getAsJsonArray().get(2).getAsFloat(), maxZ, 0.001f);
    }

    public void assertAABBi(JsonElement element, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        JsonObject m1 = element.getAsJsonObject();
        assertTrue(m1.has("min"));
        assertEquals(m1.get("min").getAsJsonArray().size(), 3);
        assertEquals(m1.get("min").getAsJsonArray().get(0).getAsInt(), minX);
        assertEquals(m1.get("min").getAsJsonArray().get(1).getAsInt(), minY);
        assertEquals(m1.get("min").getAsJsonArray().get(2).getAsInt(), minZ);
        assertTrue(m1.has("max"));
        assertEquals(m1.get("max").getAsJsonArray().size(), 3);
        assertEquals(m1.get("max").getAsJsonArray().get(0).getAsInt(), maxX);
        assertEquals(m1.get("max").getAsJsonArray().get(1).getAsInt(), maxY);
        assertEquals(m1.get("max").getAsJsonArray().get(2).getAsInt(), maxZ);
    }

    public void assertBlockRegion(JsonElement element, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        JsonObject m1 = element.getAsJsonObject();
        assertTrue(m1.has("min"));
        assertEquals(m1.get("min").getAsJsonArray().size(), 3);
        assertEquals(m1.get("min").getAsJsonArray().get(0).getAsInt(), minX);
        assertEquals(m1.get("min").getAsJsonArray().get(1).getAsInt(), minY);
        assertEquals(m1.get("min").getAsJsonArray().get(2).getAsInt(), minZ);
        assertTrue(m1.has("max"));
        assertEquals(m1.get("max").getAsJsonArray().size(), 3);
        assertEquals(m1.get("max").getAsJsonArray().get(0).getAsInt(), maxX);
        assertEquals(m1.get("max").getAsJsonArray().get(1).getAsInt(), maxY);
        assertEquals(m1.get("max").getAsJsonArray().get(2).getAsInt(), maxZ);
    }
}

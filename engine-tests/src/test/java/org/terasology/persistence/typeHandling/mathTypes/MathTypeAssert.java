// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.persistence.typeHandling.mathTypes;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.junit.Assert;

public class MathTypeAssert {

    public void assertAABBf(JsonElement element, float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        JsonObject m1 = element.getAsJsonObject();
        Assert.assertTrue(m1.has("min"));
        Assert.assertEquals(m1.get("min").getAsJsonArray().size(), 3);
        Assert.assertEquals(m1.get("min").getAsJsonArray().get(0).getAsFloat(), minX, 0.001f);
        Assert.assertEquals(m1.get("min").getAsJsonArray().get(1).getAsFloat(), minY, 0.001f);
        Assert.assertEquals(m1.get("min").getAsJsonArray().get(2).getAsFloat(), minZ, 0.001f);
        Assert.assertTrue(m1.has("max"));
        Assert.assertEquals(m1.get("max").getAsJsonArray().size(), 3);
        Assert.assertEquals(m1.get("max").getAsJsonArray().get(0).getAsFloat(), maxX, 0.001f);
        Assert.assertEquals(m1.get("max").getAsJsonArray().get(1).getAsFloat(), maxY, 0.001f);
        Assert.assertEquals(m1.get("max").getAsJsonArray().get(2).getAsFloat(), maxZ, 0.001f);
    }

    public void assertAABBi(JsonElement element, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        JsonObject m1 = element.getAsJsonObject();
        Assert.assertTrue(m1.has("min"));
        Assert.assertEquals(m1.get("min").getAsJsonArray().size(), 3);
        Assert.assertEquals(m1.get("min").getAsJsonArray().get(0).getAsInt(), minX);
        Assert.assertEquals(m1.get("min").getAsJsonArray().get(1).getAsInt(), minY);
        Assert.assertEquals(m1.get("min").getAsJsonArray().get(2).getAsInt(), minZ);
        Assert.assertTrue(m1.has("max"));
        Assert.assertEquals(m1.get("max").getAsJsonArray().size(), 3);
        Assert.assertEquals(m1.get("max").getAsJsonArray().get(0).getAsInt(), maxX);
        Assert.assertEquals(m1.get("max").getAsJsonArray().get(1).getAsInt(), maxY);
        Assert.assertEquals(m1.get("max").getAsJsonArray().get(2).getAsInt(), maxZ);
    }

    public void assertBlockRegion(JsonElement element, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        JsonObject m1 = element.getAsJsonObject();
        Assert.assertTrue(m1.has("min"));
        Assert.assertEquals(m1.get("min").getAsJsonArray().size(), 3);
        Assert.assertEquals(m1.get("min").getAsJsonArray().get(0).getAsInt(), minX);
        Assert.assertEquals(m1.get("min").getAsJsonArray().get(1).getAsInt(), minY);
        Assert.assertEquals(m1.get("min").getAsJsonArray().get(2).getAsInt(), minZ);
        Assert.assertTrue(m1.has("max"));
        Assert.assertEquals(m1.get("max").getAsJsonArray().size(), 3);
        Assert.assertEquals(m1.get("max").getAsJsonArray().get(0).getAsInt(), maxX);
        Assert.assertEquals(m1.get("max").getAsJsonArray().get(1).getAsInt(), maxY);
        Assert.assertEquals(m1.get("max").getAsJsonArray().get(2).getAsInt(), maxZ);
    }
}

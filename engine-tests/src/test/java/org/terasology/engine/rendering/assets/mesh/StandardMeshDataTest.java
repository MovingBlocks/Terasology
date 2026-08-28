// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.assets.mesh;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;
import org.terasology.joml.test.VectorAssert;
import org.terasology.nui.Color;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StandardMeshDataTest {

    @Test
    public void combineStandardMeshData() {
        StandardMeshData m1 = new StandardMeshData();
        m1.position.put(new Vector3f(10f, 10f, 10f));
        m1.position.put(new Vector3f(15f, 20f, 10f));
        m1.position.put(new Vector3f(10f, 30f, 10f));

        StandardMeshData m2 = new StandardMeshData();
        m2.position.put(new Vector3f(10f, 10f, 10f));
        m2.position.put(new Vector3f(15f, 20f, 10f));
        m2.position.put(new Vector3f(10f, 30f, 10f));

        m1.combine(new Matrix4f(), m2);

        assertEquals(m1.position.getPosition(), 6);
        assertEquals(m1.normal.getPosition(), 0);
        assertEquals(m1.uv0.getPosition(), 0);
        assertEquals(m1.uv1.getPosition(), 0);
        assertEquals(m1.color0.getPosition(), 0);

        VectorAssert.assertEquals(m1.position.get(0, new Vector3f()), new Vector3f(10f, 10f, 10f), .001f);
        VectorAssert.assertEquals(m1.position.get(1, new Vector3f()), new Vector3f(15f, 20f, 10f), .001f);
        VectorAssert.assertEquals(m1.position.get(2, new Vector3f()), new Vector3f(10f, 30f, 10f), .001f);

        VectorAssert.assertEquals(m1.position.get(3, new Vector3f()), new Vector3f(10f, 10f, 10f), .001f);
        VectorAssert.assertEquals(m1.position.get(4, new Vector3f()), new Vector3f(15f, 20f, 10f), .001f);
        VectorAssert.assertEquals(m1.position.get(5, new Vector3f()), new Vector3f(10f, 30f, 10f), .001f);

    }

    @Test
    public void combineStandardMeshDataWithTransform() {
        StandardMeshData m1 = new StandardMeshData();
        m1.position.put(new Vector3f(-1f, 1f, 0f));
        m1.position.put(new Vector3f(1f, 1f, 0f));
        m1.position.put(new Vector3f(1f, -1f, 0f));
        m1.position.put(new Vector3f(-1f, -1f, 0f));

        StandardMeshData m2 = new StandardMeshData();
        m2.position.put(new Vector3f(-1f, 1f, 0f));
        m2.position.put(new Vector3f(1f, 1f, 0f));
        m2.position.put(new Vector3f(1f, -1f, 0f));
        m2.position.put(new Vector3f(-1f, -1f, 0f));

        m1.combine(new Matrix4f().translate(10f,0,0), m2);

        VectorAssert.assertEquals(m1.position.get(0, new Vector3f()), new Vector3f(-1f, 1f, 0f), .001f);
        VectorAssert.assertEquals(m1.position.get(1, new Vector3f()), new Vector3f(1f, 1f, 0f), .001f);
        VectorAssert.assertEquals(m1.position.get(2, new Vector3f()), new Vector3f(1f, -1f, 0f), .001f);
        VectorAssert.assertEquals(m1.position.get(3, new Vector3f()), new Vector3f(-1f, -1f, 0f), .001f);

        VectorAssert.assertEquals(m1.position.get(4, new Vector3f()), new Vector3f(9f, 1f, 0), .001f);
        VectorAssert.assertEquals(m1.position.get(5, new Vector3f()), new Vector3f(11f, 1f, 0), .001f);
        VectorAssert.assertEquals(m1.position.get(6, new Vector3f()), new Vector3f(11f, -1f, 0), .001f);
        VectorAssert.assertEquals(m1.position.get(7, new Vector3f()), new Vector3f(9f, -1f, 0), .001f);
    }

    @Test
    public void secondMeshWithColorCombineStandardMeshData() {
        StandardMeshData m1 = new StandardMeshData();
        m1.position.put(new Vector3f(10f, 10f, 10f));
        m1.position.put(new Vector3f(15f, 20f, 10f));
        m1.position.put(new Vector3f(10f, 30f, 10f));

        StandardMeshData m2 = new StandardMeshData();
        m2.position.put(new Vector3f(10f, 10f, 10f));
        m2.position.put(new Vector3f(15f, 20f, 10f));
        m2.position.put(new Vector3f(10f, 30f, 10f));
        m2.color0.put(Color.blue);
        m2.color0.put(Color.blue);
        m2.color0.put(Color.blue);

        m1.combine(new Matrix4f(), m2);

        assertEquals(m1.position.getPosition(), 6);
        assertEquals(m1.color0.getPosition(), 6);
        assertEquals(m1.normal.getPosition(), 0);
        assertEquals(m1.uv0.getPosition(), 0);
        assertEquals(m1.uv1.getPosition(), 0);

        VectorAssert.assertEquals(m1.position.get(0, new Vector3f()), new Vector3f(10f, 10f, 10f), .001f);
        VectorAssert.assertEquals(m1.position.get(1, new Vector3f()), new Vector3f(15f, 20f, 10f), .001f);
        VectorAssert.assertEquals(m1.position.get(2, new Vector3f()), new Vector3f(10f, 30f, 10f), .001f);
        assertEquals(m1.color0.get(0, new Color()), new Color(Color.transparent));
        assertEquals(m1.color0.get(1, new Color()), new Color(Color.transparent));
        assertEquals(m1.color0.get(2, new Color()), new Color(Color.transparent));

        VectorAssert.assertEquals(m1.position.get(3, new Vector3f()), new Vector3f(10f, 10f, 10f), .001f);
        VectorAssert.assertEquals(m1.position.get(4, new Vector3f()), new Vector3f(15f, 20f, 10f), .001f);
        VectorAssert.assertEquals(m1.position.get(5, new Vector3f()), new Vector3f(10f, 30f, 10f), .001f);
        assertEquals(m1.color0.get(3, new Color()), new Color(Color.blue));
        assertEquals(m1.color0.get(4, new Color()), new Color(Color.blue));
        assertEquals(m1.color0.get(5, new Color()), new Color(Color.blue));
    }
}

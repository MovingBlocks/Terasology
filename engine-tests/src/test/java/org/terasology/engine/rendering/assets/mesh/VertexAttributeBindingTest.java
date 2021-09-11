// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.assets.mesh;

import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.junit.Test;
import org.terasology.engine.rendering.assets.mesh.resource.GLAttributes;
import org.terasology.engine.rendering.assets.mesh.resource.VertexAttributeBinding;
import org.terasology.engine.rendering.assets.mesh.resource.VertexFloatAttributeBinding;
import org.terasology.engine.rendering.assets.mesh.resource.VertexIntegerAttributeBinding;
import org.terasology.engine.rendering.assets.mesh.resource.VertexResourceBuilder;
import org.terasology.joml.test.VectorAssert;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class VertexAttributeBindingTest {
    @Test
    public void testPutAllBindingVector() {
        VertexResourceBuilder builder = new VertexResourceBuilder();
        VertexAttributeBinding<Vector3fc, Vector3f> a1 = builder.add(0, GLAttributes.VECTOR_3_F_VERTEX_ATTRIBUTE);
        builder.build();

        a1.putAll(
                new Vector3f(10, 5, 2),
                new Vector3f(2, 15, 2),
                new Vector3f(1, 5, 13),
                new Vector3f(1, 1, 1)
        );

        VectorAssert.assertEquals(new Vector3f(10, 5, 2), a1.get(0, new Vector3f()), 0.001f);
        VectorAssert.assertEquals(new Vector3f(2, 15, 2), a1.get(1, new Vector3f()), 0.001f);
        VectorAssert.assertEquals(new Vector3f(1, 5, 13), a1.get(2, new Vector3f()), 0.001f);
        VectorAssert.assertEquals(new Vector3f(1, 1, 1), a1.get(3, new Vector3f()), 0.001f);
    }

    @Test
    public void testPutAllBindingFloat() {
        VertexResourceBuilder builder = new VertexResourceBuilder();
        VertexFloatAttributeBinding a1 = builder.add(0, GLAttributes.FLOAT_1_VERTEX_ATTRIBUTE);
        builder.build();

        a1.putAll(10f, .5f, 12.5f, 13.5f);

        assertEquals(10f, a1.get(0), 0.001f);
        assertEquals(.5f, a1.get(1), 0.001f);
        assertEquals(12.5f, a1.get(2), 0.001f);
        assertEquals(13.5f, a1.get(3), 0.001f);
    }

    @Test
    public void testPutAllBindingInt() {
        VertexResourceBuilder builder = new VertexResourceBuilder();
        VertexIntegerAttributeBinding a1 = builder.add(0, GLAttributes.INT_1_VERTEX_ATTRIBUTE);
        builder.build();

        a1.putAll(10, 2, 1, 1);

        assertEquals(10, a1.get(0));
        assertEquals(2, a1.get(1));
        assertEquals(1, a1.get(2));
        assertEquals(1, a1.get(3));
    }
}

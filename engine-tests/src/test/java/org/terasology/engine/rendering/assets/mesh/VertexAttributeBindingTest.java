// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.assets.mesh;

import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.junit.Test;
import org.terasology.engine.rendering.assets.mesh.resource.GLAttributes;
import org.terasology.engine.rendering.assets.mesh.resource.VertexAttributeBinding;
import org.terasology.engine.rendering.assets.mesh.resource.VertexResourceBuilder;
import org.terasology.joml.test.VectorAssert;

public class VertexAttributeBindingTest {
    @Test
    public void testPutAllBinding() {
        VertexResourceBuilder builder = new VertexResourceBuilder();
        VertexAttributeBinding<Vector3fc, Vector3f> a1 = builder.add(0, GLAttributes.VECTOR_3_F_VERTEX_ATTRIBUTE);
        builder.build();

        a1.putAll(new Vector3f[]{
                new Vector3f(10, 5, 2),
                new Vector3f(2, 15, 2),
                new Vector3f(1, 5, 13),
                new Vector3f(1, 1, 1)
        });

        VectorAssert.assertEquals(new Vector3f(10, 5, 2), a1.get(0, new Vector3f()), 0.001f);
        VectorAssert.assertEquals(new Vector3f(2, 15, 2), a1.get(1, new Vector3f()), 0.001f);
        VectorAssert.assertEquals(new Vector3f(1, 5, 13), a1.get(2, new Vector3f()), 0.001f);
        VectorAssert.assertEquals(new Vector3f(1, 1, 1), a1.get(3, new Vector3f()), 0.001f);
    }

}

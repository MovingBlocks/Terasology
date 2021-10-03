// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.assets.mesh;

import com.google.common.primitives.UnsignedBytes;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.junit.Test;
import org.terasology.engine.rendering.assets.mesh.resource.GLAttributes;
import org.terasology.engine.rendering.assets.mesh.resource.VertexAttributeBinding;
import org.terasology.engine.rendering.assets.mesh.resource.VertexByteAttributeBinding;
import org.terasology.engine.rendering.assets.mesh.resource.VertexIntegerAttributeBinding;
import org.terasology.engine.rendering.assets.mesh.resource.VertexResource;
import org.terasology.engine.rendering.assets.mesh.resource.VertexResourceBuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class VertexResourceTest {
    @Test
    public void testReserveVertexResource() {
        VertexResourceBuilder builder = new VertexResourceBuilder();
        VertexAttributeBinding<Vector3fc, Vector3f> a1 = builder.add(0, GLAttributes.VECTOR_3_F_VERTEX_ATTRIBUTE);
        VertexResource resource = builder.build();

        a1.reserve(10);
        a1.put(new Vector3f(10, 10, 10));
        a1.put(new Vector3f(5, 10, 10));
        a1.put(new Vector3f(15, 10, 10));

        assertEquals(1, a1.getPosition());

        resource.writeBuffer(buffer -> {
            assertNotNull(buffer);
            assertEquals(Float.BYTES * 3 * 10, buffer.capacity());
            assertEquals(Float.BYTES * 3 * 3, buffer.limit());

            int index = 0;
            assertEquals(10.0f, buffer.getFloat((index++) * Float.BYTES), 0.001f);
            assertEquals(10.0f, buffer.getFloat((index++) * Float.BYTES), 0.001f);
            assertEquals(10.0f, buffer.getFloat((index++) * Float.BYTES), 0.001f);

            assertEquals(5.0f, buffer.getFloat((index++) * Float.BYTES), 0.001f);
            assertEquals(10.0f, buffer.getFloat((index++) * Float.BYTES), 0.001f);
            assertEquals(10.0f, buffer.getFloat((index++) * Float.BYTES), 0.001f);

            assertEquals(15.0f, buffer.getFloat((index++) * Float.BYTES), 0.001f);
            assertEquals(10.0f, buffer.getFloat((index++) * Float.BYTES), 0.001f);
            assertEquals(10.0f, buffer.getFloat((index++) * Float.BYTES), 0.001f);
        });
    }

    @Test
    public void testAllocation() {
        VertexResourceBuilder builder = new VertexResourceBuilder();
        VertexAttributeBinding<Vector3fc, Vector3f> a1 = builder.add(0, GLAttributes.VECTOR_3_F_VERTEX_ATTRIBUTE);
        VertexByteAttributeBinding a2 = builder.add(0, GLAttributes.BYTE_1_VERTEX_ATTRIBUTE);
        VertexResource resource = builder.build();
        a1.allocate(10);
        int stride = (Float.BYTES * 3) + Byte.BYTES;
        resource.writeBuffer(buffer -> {
            assertEquals(buffer.limit(), stride * 10);
            assertEquals(buffer.capacity(), stride * 10);
        });
    }

    @Test
    public void testInterleave() {
        VertexResourceBuilder builder = new VertexResourceBuilder();
        VertexAttributeBinding<Vector3fc, Vector3f> a1 = builder.add(0, GLAttributes.VECTOR_3_F_VERTEX_ATTRIBUTE);
        VertexByteAttributeBinding a2 = builder.add(0, GLAttributes.BYTE_1_VERTEX_ATTRIBUTE);
        VertexResource resource = builder.build();

        a1.put(new Vector3f(10, 0, -4));
        a2.put(UnsignedBytes.checkedCast(2));
        a2.put(UnsignedBytes.checkedCast(10));

        assertEquals(2, a2.getPosition());
        assertEquals(1, a1.getPosition());

        int stride = (Float.BYTES * 3) + Byte.BYTES;
        resource.writeBuffer(buffer -> {
            assertEquals(buffer.limit(), stride * 2);

            assertEquals(10, buffer.getFloat((0 * Float.BYTES)), 0.001f);
            assertEquals(0, buffer.getFloat((1 * Float.BYTES)), 0.001f);
            assertEquals(-4, buffer.getFloat(2 * Float.BYTES), 0.001f);
            assertEquals(2, buffer.get((3 * Float.BYTES)));

            assertEquals(0, buffer.getFloat(stride + (0 * Float.BYTES)), 0.001f);
            assertEquals(0, buffer.getFloat(stride + (1 * Float.BYTES)), 0.001f);
            assertEquals(0, buffer.getFloat(stride + (2 * Float.BYTES)), 0.001f);
            assertEquals(10, buffer.get(stride + (3 * Float.BYTES)));
        });
    }

    @Test
    public void testRewind() {
        VertexResourceBuilder builder = new VertexResourceBuilder();
        VertexIntegerAttributeBinding a1 = builder.add(0, GLAttributes.INT_1_VERTEX_ATTRIBUTE);
        VertexResource resource = builder.build();

        a1.put(10);
        a1.put(20);

        assertEquals(2, a1.getPosition());
        resource.writeBuffer(buffer -> {
            assertEquals(2 * Integer.BYTES, buffer.limit());

            assertEquals(10, buffer.getInt(Integer.BYTES * 0));
            assertEquals(20, buffer.getInt(Integer.BYTES * 1));
        });

        a1.rewind(); // rewind buffer
        assertEquals(0, a1.getPosition()); // position is at 0
        a1.put(15); // set 15 for position 0
        resource.writeBuffer(buffer -> {
            assertEquals(2 * Integer.BYTES, buffer.limit());

            assertEquals(15, buffer.getInt(Integer.BYTES * 0));
            assertEquals(20, buffer.getInt(Integer.BYTES * 1));
        });

        a1.put(5); // set 5 for position 1
        resource.writeBuffer(buffer -> {
            assertEquals(2 * Integer.BYTES, buffer.limit());

            assertEquals(15, buffer.getInt(Integer.BYTES * 0));
            assertEquals(5, buffer.getInt(Integer.BYTES * 1));
        });
    }

}

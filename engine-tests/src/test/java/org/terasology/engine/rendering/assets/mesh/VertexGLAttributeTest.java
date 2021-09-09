// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.assets.mesh;

import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.junit.Test;
import org.terasology.engine.rendering.assets.mesh.resource.GLAttributes;
import org.terasology.engine.rendering.assets.mesh.resource.VertexAttributeBinding;
import org.terasology.engine.rendering.assets.mesh.resource.VertexFloatAttributeBinding;
import org.terasology.engine.rendering.assets.mesh.resource.VertexIntegerAttributeBinding;
import org.terasology.engine.rendering.assets.mesh.resource.VertexResource;
import org.terasology.engine.rendering.assets.mesh.resource.VertexResourceBuilder;
import org.terasology.nui.Color;
import org.terasology.nui.Colorc;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class VertexGLAttributeTest {

    @Test
    public void testFloatBinding() {
        VertexResourceBuilder builder = new VertexResourceBuilder();
        VertexFloatAttributeBinding a1 = builder.add(0, GLAttributes.FLOAT_1_VERTEX_ATTRIBUTE);
        VertexResource resource = builder.build();

        a1.put(10.0f);
        a1.put(15.0f);
        a1.put(15.5f);
        a1.put(2.0f);
        a1.put(-100.0f);

        assertEquals(5, a1.getPosition());
        resource.writeBuffer(buffer -> {
            assertEquals(5 * Float.BYTES, buffer.limit());

            assertEquals(10.0f, buffer.getFloat(Float.BYTES * 0), 0.0001f);
            assertEquals(15.0f, buffer.getFloat(Float.BYTES * 1), 0.0001f);
            assertEquals(15.5f, buffer.getFloat(Float.BYTES * 2), 0.0001f);
            assertEquals(2.0f, buffer.getFloat(Float.BYTES * 3), 0.0001f);
            assertEquals(-100.0f, buffer.getFloat(Float.BYTES * 4), 0.0001f);
        });
    }

    @Test
    public void testIntBinding() {
        VertexResourceBuilder builder = new VertexResourceBuilder();
        VertexIntegerAttributeBinding a1 = builder.add(0, GLAttributes.INT_1_VERTEX_ATTRIBUTE);
        VertexResource resource = builder.build();

        a1.put(10);
        a1.put(20);
        a1.put(150);
        a1.put(100);
        a1.put(-100);

        assertEquals(5, a1.getPosition());
        resource.writeBuffer(buffer -> {
            assertEquals(5 * Integer.BYTES, buffer.limit());

            assertEquals(10, buffer.getInt(Integer.BYTES * 0));
            assertEquals(20, buffer.getInt(Integer.BYTES * 1));
            assertEquals(150, buffer.getInt(Integer.BYTES * 2));
            assertEquals(100, buffer.getInt(Integer.BYTES * 3));
            assertEquals(-100, buffer.getInt(Integer.BYTES * 4));
        });
    }

    @Test
    public void testByteBinding() {
        VertexResourceBuilder builder = new VertexResourceBuilder();
        VertexIntegerAttributeBinding a1 = builder.add(0, GLAttributes.BYTE_1_VERTEX_ATTRIBUTE);
        VertexResource resource = builder.build();

        a1.put(10);
        a1.put(150);
        a1.put(300);
        a1.put(100);

        assertEquals(4, a1.getPosition());
        resource.writeBuffer(buffer -> {
            assertEquals(4 * Byte.BYTES, buffer.limit());

            assertEquals(10, Byte.toUnsignedInt(buffer.get(Byte.BYTES * 0)));
            assertEquals(150, Byte.toUnsignedInt(buffer.get(Byte.BYTES * 1)));
            assertEquals(255, Byte.toUnsignedInt(buffer.get(Byte.BYTES * 2)));
            assertEquals(100, Byte.toUnsignedInt(buffer.get(Byte.BYTES * 3)));
        });
    }

    @Test
    public void testVector3fBinding() {
        VertexResourceBuilder builder = new VertexResourceBuilder();
        VertexAttributeBinding<Vector3fc, Vector3f> a1 = builder.add(0, GLAttributes.VECTOR_3_F_VERTEX_ATTRIBUTE);
        VertexResource resource = builder.build();

        a1.put(new Vector3f(10, 150, 1.5f));
        a1.put(new Vector3f(15.1f, 15.04f, 15.5f));
        a1.put(new Vector3f(16f, 150, 31.5f));

        assertEquals(3, a1.getPosition());
        int stride = Float.BYTES * 3;
        resource.writeBuffer(buffer -> {
            assertEquals(3 * 3 * Float.BYTES, buffer.limit());

            assertEquals(10, buffer.getFloat(Float.BYTES * 0), 0.001f);
            assertEquals(150, buffer.getFloat(Float.BYTES * 1), 0.001f);
            assertEquals(1.5f, buffer.getFloat(Float.BYTES * 2), 0.001f);

            assertEquals(15.1f, buffer.getFloat((stride) + Float.BYTES * 0), 0.001f);
            assertEquals(15.04f, buffer.getFloat((stride) + Float.BYTES * 1), 0.001f);
            assertEquals(15.5f, buffer.getFloat((stride) + Float.BYTES * 2), 0.001f);

            assertEquals(16f, buffer.getFloat((stride * 2) + Float.BYTES * 0), 0.001f);
            assertEquals(150f, buffer.getFloat((stride * 2) + Float.BYTES * 1), 0.001f);
            assertEquals(31.5f, buffer.getFloat((stride * 2) + Float.BYTES * 2), 0.001f);
        });
    }

    @Test
    public void testVector4fBinding() {
        VertexResourceBuilder builder = new VertexResourceBuilder();
        VertexAttributeBinding<Vector4fc, Vector4f> a1 = builder.add(0, GLAttributes.VECTOR_4_F_VERTEX_ATTRIBUTE);
        VertexResource resource = builder.build();

        a1.put(new Vector4f(10, 150, -10, 12));
        a1.put(new Vector4f(15.1f, 15.04f, 10, 12));
        a1.put(new Vector4f(16f, 150, -10, 12));

        assertEquals(3, a1.getPosition());
        int stride = Float.BYTES * 4;
        resource.writeBuffer(buffer -> {
            assertEquals(3 * 4 * Float.BYTES, buffer.limit());

            assertEquals(10, buffer.getFloat(Float.BYTES * 0), 0.001f);
            assertEquals(150, buffer.getFloat(Float.BYTES * 1), 0.001f);
            assertEquals(-10, buffer.getFloat(Float.BYTES * 2), 0.001f);
            assertEquals(12, buffer.getFloat(Float.BYTES * 3), 0.001f);

            assertEquals(15.1f, buffer.getFloat((stride) + Float.BYTES * 0), 0.001f);
            assertEquals(15.04f, buffer.getFloat((stride) + Float.BYTES * 1), 0.001f);
            assertEquals(10, buffer.getFloat((stride) + Float.BYTES * 2), 0.001f);
            assertEquals(12, buffer.getFloat((stride) + Float.BYTES * 3), 0.001f);

            assertEquals(16f, buffer.getFloat((stride * 2) + Float.BYTES * 0), 0.001f);
            assertEquals(150f, buffer.getFloat((stride * 2) + Float.BYTES * 1), 0.001f);
            assertEquals(-10, buffer.getFloat((stride * 2) + Float.BYTES * 2), 0.001f);
            assertEquals(12, buffer.getFloat((stride * 2) + Float.BYTES * 3), 0.001f);
        });
    }

    @Test
    public void testVector2fBinding() {
        VertexResourceBuilder builder = new VertexResourceBuilder();
        VertexAttributeBinding<Vector2fc, Vector2f> a1 = builder.add(0, GLAttributes.VECTOR_2_F_VERTEX_ATTRIBUTE);
        VertexResource resource = builder.build();

        a1.put(new Vector2f(10, 150));
        a1.put(new Vector2f(15.1f, 15.04f));
        a1.put(new Vector2f(16f, 150));

        assertEquals(3, a1.getPosition());
        int stride = Float.BYTES * 2;
        resource.writeBuffer(buffer -> {
            assertEquals(3 * 2 * Float.BYTES, buffer.limit());

            assertEquals(10, buffer.getFloat(Float.BYTES * 0), 0.001f);
            assertEquals(150, buffer.getFloat(Float.BYTES * 1), 0.001f);

            assertEquals(15.1f, buffer.getFloat((stride) + Float.BYTES * 0), 0.001f);
            assertEquals(15.04f, buffer.getFloat((stride) + Float.BYTES * 1), 0.001f);

            assertEquals(16f, buffer.getFloat((stride * 2) + Float.BYTES * 0), 0.001f);
            assertEquals(150f, buffer.getFloat((stride * 2) + Float.BYTES * 1), 0.001f);
        });
    }


    @Test
    public void testColorPackedBinding() {
        VertexResourceBuilder builder = new VertexResourceBuilder();
        VertexAttributeBinding<Colorc, Color> a1 = builder.add(0, GLAttributes.COLOR_4_PACKED_VERTEX_ATTRIBUTE);
        VertexResource resource = builder.build();

        a1.put(Color.white);
        a1.put(Color.red);
        a1.put(Color.green);
        a1.put(Color.blue);

        assertEquals(4, a1.getPosition());
        int stride = Byte.BYTES * 4;
        resource.writeBuffer(buffer -> {
            assertEquals(4 * 4 * Byte.BYTES, buffer.limit());

            assertEquals(255, Byte.toUnsignedInt(buffer.get(Byte.BYTES * 0)));
            assertEquals(255, Byte.toUnsignedInt(buffer.get(Byte.BYTES * 1)));
            assertEquals(255, Byte.toUnsignedInt(buffer.get(Byte.BYTES * 2)));
            assertEquals(255, Byte.toUnsignedInt(buffer.get(Byte.BYTES * 3)));

            assertEquals(255, Byte.toUnsignedInt(buffer.get(stride + Byte.BYTES * 0)));
            assertEquals(0, Byte.toUnsignedInt(buffer.get(stride + Byte.BYTES * 1)));
            assertEquals(0, Byte.toUnsignedInt(buffer.get(stride + Byte.BYTES * 2)));
            assertEquals(255, Byte.toUnsignedInt(buffer.get(stride + Byte.BYTES * 3)));

            assertEquals(0, Byte.toUnsignedInt(buffer.get((2 * stride) + Byte.BYTES * 0)));
            assertEquals(255, Byte.toUnsignedInt(buffer.get((2 * stride) + Byte.BYTES * 1)));
            assertEquals(0, Byte.toUnsignedInt(buffer.get((2 * stride) + Byte.BYTES * 2)));
            assertEquals(255, Byte.toUnsignedInt(buffer.get((2 * stride) + Byte.BYTES * 3)));

            assertEquals(0, Byte.toUnsignedInt(buffer.get((3 * stride) + Byte.BYTES * 0)));
            assertEquals(0, Byte.toUnsignedInt(buffer.get((3 * stride) + Byte.BYTES * 1)));
            assertEquals(255, Byte.toUnsignedInt(buffer.get((3 * stride) + Byte.BYTES * 2)));
            assertEquals(255, Byte.toUnsignedInt(buffer.get((3 * stride) + Byte.BYTES * 3)));
        });
    }

    @Test
    public void testColorBinding() {
        VertexResourceBuilder builder = new VertexResourceBuilder();
        VertexAttributeBinding<Colorc, Color> a1 = builder.add(0, GLAttributes.COLOR_4_F_VERTEX_ATTRIBUTE);
        VertexResource resource = builder.build();

        a1.put(Color.white);
        a1.put(Color.red);
        a1.put(Color.green);
        a1.put(Color.blue);

        assertEquals(4, a1.getPosition());
        int stride = Float.BYTES * 4;
        resource.writeBuffer(buffer -> {
            assertEquals(4 * 4 * Float.BYTES, buffer.limit());

            assertEquals(1.0f, buffer.getFloat(Float.BYTES * 0), 0.001f);
            assertEquals(1.0f, buffer.getFloat(Float.BYTES * 1), 0.001f);
            assertEquals(1.0f, buffer.getFloat(Float.BYTES * 2), 0.001f);
            assertEquals(1.0f, buffer.getFloat(Float.BYTES * 3), 0.001f);

            assertEquals(1.0f, buffer.getFloat(stride + Float.BYTES * 0));
            assertEquals(0, buffer.getFloat(stride + Float.BYTES * 1));
            assertEquals(0, buffer.getFloat(stride + Float.BYTES * 2));
            assertEquals(1.0f, buffer.getFloat(stride + Float.BYTES * 3));

            assertEquals(0, buffer.getFloat((2 * stride) + Float.BYTES * 0));
            assertEquals(1.0f, buffer.getFloat((2 * stride) + Float.BYTES * 1));
            assertEquals(0, buffer.getFloat((2 * stride) + Float.BYTES * 2));
            assertEquals(1.0f, buffer.getFloat((2 * stride) + Float.BYTES * 3));

            assertEquals(0, buffer.getFloat((3 * stride) + Float.BYTES * 0));
            assertEquals(0, buffer.getFloat((3 * stride) + Float.BYTES * 1));
            assertEquals(1.0f, buffer.getFloat((3 * stride) + Float.BYTES * 2));
            assertEquals(1.0f, buffer.getFloat((3 * stride) + Float.BYTES * 3));
        });
    }
}

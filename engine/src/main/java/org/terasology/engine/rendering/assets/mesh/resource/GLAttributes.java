// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.assets.mesh.resource;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.terasology.nui.Color;

import java.nio.ByteBuffer;

public final class GLAttributes {
    private GLAttributes() {

    }

    public static final VertexAttribute<Vector3f> VECTOR_3_F_VERTEX_ATTRIBUTE = new VertexAttribute<>(Vector3f.class, new VertexAttribute.AttributeConfiguration<Vector3f>() {

        @Override
        public void write(Vector3f value, int vertIdx, int offset, VertexResource resource) {
            int bufferStart = vertIdx * resource.getInStride() + offset;
            ByteBuffer buffer = resource.buffer();
            buffer.putFloat(bufferStart, value.x());
            buffer.putFloat(bufferStart + Float.BYTES, value.y());
            buffer.putFloat(bufferStart + Float.BYTES * 2, value.z());
        }

        @Override
        public Vector3f read(int vertIdx, int offset, VertexResource resource, Vector3f dest) {
            int bufferStart = vertIdx * resource.getInStride() + offset;
            ByteBuffer buffer = resource.buffer();
            dest.x = buffer.getFloat(bufferStart);
            dest.y = buffer.getFloat(bufferStart + Float.BYTES);
            dest.z = buffer.getFloat(bufferStart + Float.BYTES * 2);
            return dest;
        }

        @Override
        public int size(int vertIdx, int offset, VertexResource resource) {
            return (vertIdx * resource.getInStride() + offset) + Float.BYTES * 3;
        }

        @Override
        public int numElements(int offset, VertexResource resource) {
            int size = (resource.getInSize() / resource.getInStride());
            if (resource.getInSize() % resource.getInStride() >= Float.BYTES * 3) {
                size++;
            }
            return size;
        }
    }, VertexAttribute.TypeMapping.ATTR_FLOAT, 3);

    public static final VertexAttribute<Vector4f> VECTOR_4_F_VERTEX_ATTRIBUTE = new VertexAttribute<>(Vector4f.class, new VertexAttribute.AttributeConfiguration<Vector4f>() {
        @Override
        public void write(Vector4f value, int vertIdx, int offset, VertexResource resource) {
            int bufferStart = vertIdx * resource.getInStride() + offset;
            ByteBuffer buffer = resource.buffer();
            buffer.putFloat(bufferStart, value.x());
            buffer.putFloat(bufferStart + Float.BYTES, value.y());
            buffer.putFloat(bufferStart + Float.BYTES * 2, value.z());
            buffer.putFloat(bufferStart + Float.BYTES * 3, value.w());
        }

        @Override
        public Vector4f read(int vertIdx, int offset, VertexResource resource, Vector4f dest) {
            int bufferStart = vertIdx * resource.getInStride() + offset;
            ByteBuffer buffer = resource.buffer();
            dest.x = buffer.getFloat(bufferStart);
            dest.y = buffer.getFloat(bufferStart + Float.BYTES);
            dest.z = buffer.getFloat(bufferStart + Float.BYTES * 2);
            dest.w = buffer.getFloat(bufferStart + Float.BYTES * 3);
            return dest;
        }

        @Override
        public int size(int vertIdx, int offset, VertexResource resource) {
            return (vertIdx * resource.getInStride() + offset) + Float.BYTES * 4;
        }

        @Override
        public int numElements(int offset, VertexResource resource) {
            int size = (resource.getInSize() / resource.getInStride());
            if (resource.getInSize() % resource.getInStride() >= Float.BYTES * 4) {
                size++;
            }
            return size;
        }
    }, VertexAttribute.TypeMapping.ATTR_FLOAT, 4);

    public static final VertexAttribute<Color> COLOR_4_F_VERTEX_ATTRIBUTE = new VertexAttribute<>(Color.class, new VertexAttribute.AttributeConfiguration<Color>() {
        @Override
        public void write(Color value, int vertIdx, int offset, VertexResource resource) {
            int bufferStart = vertIdx * resource.getInStride() + offset;
            ByteBuffer buffer = resource.buffer();

            buffer.putFloat(bufferStart, value.rf());
            buffer.putFloat(bufferStart + Float.BYTES, value.gf());
            buffer.putFloat(bufferStart + Float.BYTES * 2, value.bf());
            buffer.putFloat(bufferStart + Float.BYTES * 3, value.af());
        }

        @Override
        public Color read(int vertIdx, int offset, VertexResource resource, Color dest) {
            int bufferStart = vertIdx * resource.getInStride() + offset;
            ByteBuffer buffer = resource.buffer();
            dest.setRed(buffer.getFloat(bufferStart));
            dest.setGreen(buffer.getFloat(bufferStart + Float.BYTES));
            dest.setBlue(buffer.getFloat(bufferStart + Float.BYTES * 2));
            dest.setAlpha(buffer.getFloat(bufferStart + Float.BYTES * 3));
            return dest;
        }

        @Override
        public int size(int vertIdx, int offset, VertexResource resource) {
            return (vertIdx * resource.getInStride() + offset) + Float.BYTES * 4;
        }

        @Override
        public int numElements(int offset, VertexResource resource) {
            int size = (resource.getInSize() / resource.getInStride());
            if (resource.getInSize() % resource.getInStride() >= Float.BYTES * 4) {
                size++;
            }
            return size;
        }

    }, VertexAttribute.TypeMapping.ATTR_FLOAT, 4);

    public static final VertexAttribute<Vector2f> VECTOR_2_F_VERTEX_ATTRIBUTE = new VertexAttribute<>(Vector2f.class, new VertexAttribute.AttributeConfiguration<Vector2f>() {
        @Override
        public void write(Vector2f value, int vertIdx, int offset, VertexResource resource) {
            int bufferStart = vertIdx * resource.getInStride() + offset;
            ByteBuffer buffer = resource.buffer();
            buffer.putFloat(bufferStart, value.x());
            buffer.putFloat(bufferStart + Float.BYTES, value.y());
        }

        @Override
        public Vector2f read(int vertIdx, int offset, VertexResource resource, Vector2f dest) {
            int bufferStart = vertIdx * resource.getInStride() + offset;
            ByteBuffer buffer = resource.buffer();
            dest.x = buffer.getFloat(bufferStart);
            dest.y = buffer.getFloat(bufferStart + Float.BYTES);
            return dest;
        }

        @Override
        public int size(int vertIdx, int offset, VertexResource resource) {
            return (vertIdx * resource.getInStride() + offset) + Float.BYTES * 2;
        }

        @Override
        public int numElements(int offset, VertexResource resource) {
            int size = (resource.getInSize() / resource.getInStride());
            if (resource.getInSize() % resource.getInStride() >= Float.BYTES * 2) {
                size++;
            }
            return size;
        }
    }, VertexAttribute.TypeMapping.ATTR_FLOAT, 2);
}

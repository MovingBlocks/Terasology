// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.assets.mesh.resource;

import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.terasology.nui.Color;
import org.terasology.nui.Colorc;

import java.nio.ByteBuffer;

public final class GLAttributes {
    private GLAttributes() {

    }

    public static final VertexAttribute<Vector3fc, Vector3f> VECTOR_3_F_VERTEX_ATTRIBUTE = new VertexAttribute<Vector3fc, Vector3f>(Vector3f.class, new VertexAttribute.AttributeConfiguration<Vector3fc, Vector3f>() {

        @Override
        public void write(Vector3fc value, int vertIdx, int offset, VertexResource resource) {
            int bufferStart = vertIdx * resource.inStride() + offset;
            ByteBuffer buffer = resource.buffer();
            buffer.putFloat(bufferStart, value.x());
            buffer.putFloat(bufferStart + Float.BYTES, value.y());
            buffer.putFloat(bufferStart + Float.BYTES * 2, value.z());
        }

        @Override
        public Vector3f read(int vertIdx, int offset, VertexResource resource, Vector3f dest) {
            int bufferStart = vertIdx * resource.inStride() + offset;
            ByteBuffer buffer = resource.buffer();
            dest.x = buffer.getFloat(bufferStart);
            dest.y = buffer.getFloat(bufferStart + Float.BYTES);
            dest.z = buffer.getFloat(bufferStart + Float.BYTES * 2);
            return dest;
        }

        @Override
        public int size(int vertIdx, int offset, VertexResource resource) {
            return (vertIdx * resource.inStride() + offset) + Float.BYTES * 3;
        }

        @Override
        public int numElements(int offset, VertexResource resource) {
            int size = (resource.inSize() / resource.inStride());
            if (resource.inSize() % resource.inStride() >= Float.BYTES * 3) {
                size++;
            }
            return size;
        }
    }, TypeMapping.ATTR_FLOAT, 3);

    public static final VertexAttribute<Vector4fc, Vector4f> VECTOR_4_F_VERTEX_ATTRIBUTE = new VertexAttribute<>(Vector4f.class, new VertexAttribute.AttributeConfiguration<Vector4fc, Vector4f>() {
        @Override
        public void write(Vector4fc value, int vertIdx, int offset, VertexResource resource) {
            int bufferStart = vertIdx * resource.inStride() + offset;
            ByteBuffer buffer = resource.buffer();
            buffer.putFloat(bufferStart, value.x());
            buffer.putFloat(bufferStart + Float.BYTES, value.y());
            buffer.putFloat(bufferStart + Float.BYTES * 2, value.z());
            buffer.putFloat(bufferStart + Float.BYTES * 3, value.w());
        }

        @Override
        public Vector4f read(int vertIdx, int offset, VertexResource resource, Vector4f dest) {
            int bufferStart = vertIdx * resource.inStride() + offset;
            ByteBuffer buffer = resource.buffer();
            dest.x = buffer.getFloat(bufferStart);
            dest.y = buffer.getFloat(bufferStart + Float.BYTES);
            dest.z = buffer.getFloat(bufferStart + Float.BYTES * 2);
            dest.w = buffer.getFloat(bufferStart + Float.BYTES * 3);
            return dest;
        }

        @Override
        public int size(int vertIdx, int offset, VertexResource resource) {
            return (vertIdx * resource.inStride() + offset) + Float.BYTES * 4;
        }

        @Override
        public int numElements(int offset, VertexResource resource) {
            int size = (resource.inSize() / resource.inStride());
            if (resource.inSize() % resource.inStride() >= Float.BYTES * 4) {
                size++;
            }
            return size;
        }
    }, TypeMapping.ATTR_FLOAT, 4);

    public static final VertexAttribute<Colorc, Color> COLOR_4_F_VERTEX_ATTRIBUTE = new VertexAttribute<>(Color.class, new VertexAttribute.AttributeConfiguration<Colorc, Color>() {
        @Override
        public void write(Colorc value, int vertIdx, int offset, VertexResource resource) {
            int bufferStart = vertIdx * resource.inStride() + offset;
            ByteBuffer buffer = resource.buffer();

            buffer.putFloat(bufferStart, value.rf());
            buffer.putFloat(bufferStart + Float.BYTES, value.gf());
            buffer.putFloat(bufferStart + Float.BYTES * 2, value.bf());
            buffer.putFloat(bufferStart + Float.BYTES * 3, value.af());
        }

        @Override
        public Color read(int vertIdx, int offset, VertexResource resource, Color dest) {
            int bufferStart = vertIdx * resource.inStride() + offset;
            ByteBuffer buffer = resource.buffer();
            dest.setRed(buffer.getFloat(bufferStart));
            dest.setGreen(buffer.getFloat(bufferStart + Float.BYTES));
            dest.setBlue(buffer.getFloat(bufferStart + Float.BYTES * 2));
            dest.setAlpha(buffer.getFloat(bufferStart + Float.BYTES * 3));
            return dest;
        }

        @Override
        public int size(int vertIdx, int offset, VertexResource resource) {
            return (vertIdx * resource.inStride() + offset) + Float.BYTES * 4;
        }

        @Override
        public int numElements(int offset, VertexResource resource) {
            int size = (resource.inSize() / resource.inStride());
            if (resource.inSize() % resource.inStride() >= (offset + Float.BYTES * 4)) {
                size++;
            }
            return size;
        }

    }, TypeMapping.ATTR_FLOAT, 4);

    public static final VertexAttribute<Vector2fc, Vector2f> VECTOR_2_F_VERTEX_ATTRIBUTE = new VertexAttribute<>(Vector2f.class, new VertexAttribute.AttributeConfiguration<Vector2fc, Vector2f>() {
        @Override
        public void write(Vector2fc value, int vertIdx, int offset, VertexResource resource) {
            int bufferStart = vertIdx * resource.inStride() + offset;
            ByteBuffer buffer = resource.buffer();
            buffer.putFloat(bufferStart, value.x());
            buffer.putFloat(bufferStart + Float.BYTES, value.y());
        }

        @Override
        public Vector2f read(int vertIdx, int offset, VertexResource resource, Vector2f dest) {
            int bufferStart = vertIdx * resource.inStride() + offset;
            ByteBuffer buffer = resource.buffer();
            dest.x = buffer.getFloat(bufferStart);
            dest.y = buffer.getFloat(bufferStart + Float.BYTES);
            return dest;
        }

        @Override
        public int size(int vertIdx, int offset, VertexResource resource) {
            return (vertIdx * resource.inStride() + offset) + Float.BYTES * 2;
        }

        @Override
        public int numElements(int offset, VertexResource resource) {
            int size = (resource.inSize() / resource.inStride());
            if (resource.inSize() % resource.inStride() >= Float.BYTES * 2) {
                size++;
            }
            return size;
        }
    }, TypeMapping.ATTR_FLOAT, 2);
}

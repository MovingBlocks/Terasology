// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.assets.mesh.resouce;

import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector3ic;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.lwjgl.opengl.GL30;
import org.terasology.nui.Color;
import org.terasology.nui.Colorc;

import java.nio.ByteBuffer;

public class VertexAttribute<TARGET> {

//    public static final VertexAttribute<Vector3ic, Integer> VECTOR_3_I_VERTEX_ATTRIBUTE = new VertexAttribute<>(
//            new AttributeConfiguration<Vector3ic, Integer>() {
//
//                @Override
//                public void map(Vector3ic value, int vertIdx, int stride, int offset, ByteBuffer buffer) {
//                    int bufferStart = vertIdx * stride + offset;
//                    buffer.putInt(bufferStart, value.x());
//                    buffer.putInt(bufferStart + Integer.BYTES, value.y());
//                    buffer.putInt(bufferStart + Integer.BYTES * 2, value.z());
//                }
//
//                @Override
//                public void map(int pos, Integer[] value, int vertIdx, int stride, int offset, ByteBuffer buffer) {
//                    int bufferStart = vertIdx * stride + offset;
//                    buffer.putInt(bufferStart, value[pos]);
//                    buffer.putInt(bufferStart + Float.BYTES, value[pos + 1]);
//                    buffer.putInt(bufferStart + Float.BYTES * 2, value[pos + 2]);
//                }
//
//                @Override
//                public void map(Vector3ic value, int vertIdx, Integer[] store) {
//                    store[vertIdx * 4] = value.x();
//                    store[vertIdx * 4 + 1] = value.y();
//                    store[vertIdx * 4 + 2] = value.z();
//                }
//
//                @Override
//                public void map(int pos, Integer[] value, int vertIdx, Integer[] store) {
//                    store[vertIdx * 4] = value[pos];
//                    store[vertIdx * 4 + 1] = value[pos + 1];
//                    store[vertIdx * 4 + 2] = value[pos + 2];
//                }
//
//                @Override
//                public Integer[] build(int size) {
//                    return new Integer[size * 3];
//                }
//            }, TypeMapping.ATTR_INT, 3);
    public static final VertexFloatAttribute<Vector3f> VECTOR_3_F_VERTEX_ATTRIBUTE = new VertexFloatAttribute<>(new VertexFloatAttribute.AttributeConfiguration<Vector3f>() {

        @Override
        public void map(Vector3f value, int vertIdx, int stride, int offset, ByteBuffer buffer) {
            int bufferStart = vertIdx * stride + offset;
            buffer.putFloat(bufferStart, value.x());
            buffer.putFloat(bufferStart + Float.BYTES, value.y());
            buffer.putFloat(bufferStart + Float.BYTES * 2, value.z());
        }

        @Override
        public void map(int pos, float[] value, int vertIdx, int stride, int offset, ByteBuffer buffer) {
            int bufferStart = vertIdx * stride + offset;
            buffer.putFloat(bufferStart, value[pos]);
            buffer.putFloat(bufferStart + Float.BYTES, value[pos + 1]);
            buffer.putFloat(bufferStart + Float.BYTES * 2, value[pos + 2]);
        }

        @Override
        public void map(Vector3f value, int vertIdx, Vector3f[] store) {
            store[vertIdx].set(value.x(), value.y(), value.z());
        }

        @Override
        public void map(int pos, float[] value, int vertIdx, Vector3f[] store) {
            store[vertIdx].set(value[pos], value[pos + 1], value[pos + 2]);
        }

        @Override
        public Vector3f[] build(int size) {
            Vector3f[] arr =  new Vector3f[size];
            for(int i = 0; i < arr.length; i++){
                arr[i] = new Vector3f();
            }
            return arr;
        }
    }, TypeMapping.ATTR_FLOAT, 3);

    public static final VertexFloatAttribute<Vector4f> VECTOR_4_F_VERTEX_ATTRIBUTE = new VertexFloatAttribute<>(new VertexFloatAttribute.AttributeConfiguration<Vector4f>() {


        @Override
        public void map(Vector4f value, int vertIdx, int stride, int offset, ByteBuffer buffer) {
            int bufferStart = vertIdx * stride + offset;
            buffer.putFloat(bufferStart, value.x());
            buffer.putFloat(bufferStart + Float.BYTES, value.y());
            buffer.putFloat(bufferStart + Float.BYTES * 2, value.z());
            buffer.putFloat(bufferStart + Float.BYTES * 3, value.w());
        }

        @Override
        public void map(int pos, float[] value, int vertIdx, int stride, int offset, ByteBuffer buffer) {
            int bufferStart = vertIdx * stride + offset;
            buffer.putFloat(bufferStart, value[pos]);
            buffer.putFloat(bufferStart + Float.BYTES, value[pos + 1]);
            buffer.putFloat(bufferStart + Float.BYTES * 2, value[pos + 2]);
            buffer.putFloat(bufferStart + Float.BYTES * 3, value[pos + 3]);
        }

        @Override
        public void map(Vector4f value, int vertIdx, Vector4f[] store) {
            store[vertIdx].set(value.x(), value.y(), value.z(), value.w());
        }

        @Override
        public void map(int pos, float[] value, int vertIdx, Vector4f[] store) {
            store[vertIdx].set(value[pos], value[pos + 1], value[pos + 2], value[pos + 3]);
        }

        @Override
        public Vector4f[] build(int size) {
            Vector4f[] arr =  new Vector4f[size];
            for(int i = 0; i < arr.length; i++){
                arr[i] = new Vector4f();
            }
            return arr;
        }
    }, TypeMapping.ATTR_FLOAT, 4);

    public static final VertexFloatAttribute<Color> COLOR_4_F_VERTEX_ATTRIBUTE = new VertexFloatAttribute<>(new VertexFloatAttribute.AttributeConfiguration<Color>() {
        @Override
        public void map(Color value, int vertIdx, int stride, int offset, ByteBuffer buffer) {
            int bufferStart = vertIdx * stride + offset;
            buffer.putFloat(bufferStart, value.rf());
            buffer.putFloat(bufferStart + Float.BYTES, value.gf());
            buffer.putFloat(bufferStart + Float.BYTES * 2, value.bf());
            buffer.putFloat(bufferStart + Float.BYTES * 3, value.af());
        }

        @Override
        public void map(int pos, float[] value, int vertIdx, int stride, int offset, ByteBuffer buffer) {
            int bufferStart = vertIdx * stride + offset;
            buffer.putFloat(bufferStart, value[pos]);
            buffer.putFloat(bufferStart + Float.BYTES, value[pos + 1]);
            buffer.putFloat(bufferStart + Float.BYTES * 2, value[pos + 2]);
            buffer.putFloat(bufferStart + Float.BYTES * 3, value[pos + 3]);
        }

        @Override
        public void map(Color value, int vertIdx, Color[] store) {
            store[vertIdx].set(value.r(), value.g(), value.b(), value.a());
        }

        @Override
        public void map(int pos, float[] value, int vertIdx, Color[] store) {
            store[vertIdx].setRed(value[pos]);
            store[vertIdx].setGreen(value[pos + 1]);
            store[vertIdx].setBlue(value[pos + 2]);
            store[vertIdx].setAlpha(value[pos + 3]);
        }

        @Override
        public Color[] build(int vertexCount) {
            Color[] arr = new Color[vertexCount];
            for(int i = 0; i < arr.length; i++){
                arr[i] = new Color();
            }
            return arr;
        }
    }, TypeMapping.ATTR_FLOAT, 4);

    public static final VertexFloatAttribute<Vector2f> VECTOR_2_F_VERTEX_ATTRIBUTE = new VertexFloatAttribute<>(new VertexFloatAttribute.AttributeConfiguration<Vector2f>() {
        @Override
        public void map(Vector2f value, int vertIdx, int stride, int offset, ByteBuffer buffer) {
            int bufferStart = vertIdx * stride + offset;
            buffer.putFloat(bufferStart, value.x());
            buffer.putFloat(bufferStart + Float.BYTES, value.y());
        }

        @Override
        public void map(int pos, float[] value, int vertIdx, int stride, int offset, ByteBuffer buffer) {
            int bufferStart = vertIdx * stride + offset;
            buffer.putFloat(bufferStart, value[pos]);
            buffer.putFloat(bufferStart + Float.BYTES, value[pos + 1]);
        }

        @Override
        public void map(Vector2f value, int vertIdx, Vector2f[] store) {
            store[vertIdx].set(value.x(), value.y());
        }

        @Override
        public void map(int pos, float[] value, int vertIdx, Vector2f[] store) {
            store[vertIdx].set(value[pos], value[pos + 1]);
        }

        @Override
        public Vector2f[] build(int size) {
            Vector2f[] arr =  new Vector2f[size];
            for(int i = 0; i < arr.length; i++){
                arr[i] = new Vector2f();
            }
            return arr;
        }

    }, TypeMapping.ATTR_FLOAT, 2);

    public final TypeMapping mapping;
    public final int count;
    protected VertexAttribute(TypeMapping mapping, int count) {
        this.mapping = mapping;
        this.count = count;
    }

    public enum TypeMapping {
        ATTR_FLOAT(Float.BYTES, GL30.GL_FLOAT),
        ATTR_SHORT(Short.BYTES, GL30.GL_SHORT),
        ATTR_BYTE(Byte.BYTES, GL30.GL_BYTE),
        ATTR_INT(Integer.BYTES, GL30.GL_INT);

        public final int size;
        public final int glType;

        TypeMapping(int size, int glType) {
            this.size = size;
            this.glType = glType;
        }
    }



}

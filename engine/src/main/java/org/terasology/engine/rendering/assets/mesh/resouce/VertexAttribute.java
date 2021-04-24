// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.assets.mesh.resouce;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL30;
import org.terasology.nui.Color;

import java.nio.ByteBuffer;

/**
 * attribute maps a target object or a primitive data to a {@link VertexResource}
 *
 * @param <TARGET> the target object
 */
public class VertexAttribute<TARGET> {

    public static final VertexFloatAttribute<Vector3f> VECTOR_3_F_VERTEX_ATTRIBUTE = new VertexFloatAttribute<>(Vector3f.class, new VertexFloatAttribute.AttributeConfiguration<Vector3f>() {

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
        public Vector3f build() {
            return new Vector3f();
        }
    }, TypeMapping.ATTR_FLOAT, 3);

    public static final VertexFloatAttribute<Vector4f> VECTOR_4_F_VERTEX_ATTRIBUTE = new VertexFloatAttribute<>(Vector4f.class, new VertexFloatAttribute.AttributeConfiguration<Vector4f>() {


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
        public Vector4f build() {
            return new Vector4f();
        }
    }, TypeMapping.ATTR_FLOAT, 4);

    public static final VertexFloatAttribute<Color> COLOR_4_F_VERTEX_ATTRIBUTE = new VertexFloatAttribute<>(Color.class, new VertexFloatAttribute.AttributeConfiguration<Color>() {
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
        public Color build() {
            return new Color();
        }
    }, TypeMapping.ATTR_FLOAT, 4);

    public static final VertexFloatAttribute<Vector2f> VECTOR_2_F_VERTEX_ATTRIBUTE = new VertexFloatAttribute<>(Vector2f.class, new VertexFloatAttribute.AttributeConfiguration<Vector2f>() {
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
        public Vector2f build() {
            return new Vector2f();
        }

    }, TypeMapping.ATTR_FLOAT, 2);

    public final TypeMapping mapping;
    public final int count;
    public final Class<TARGET> type;

    /**
     * @param type the mapping type
     * @param mapping maps a primitive to a given supported type.
     * @param count the number elements that is described by the target
     */
    protected VertexAttribute(Class<TARGET> type,TypeMapping mapping, int count) {
        this.type = type;
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

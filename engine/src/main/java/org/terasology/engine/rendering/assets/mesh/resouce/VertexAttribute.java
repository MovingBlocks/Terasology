// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.assets.mesh.resouce;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL30;
import org.terasology.nui.Color;

import java.nio.ByteBuffer;
import java.util.Optional;

/**
 * attribute maps a target object or a primitive data to a {@link VertexResource}
 *
 * @param <TARGET> the target object
 */
public class VertexAttribute<TARGET> {

    public static final VertexFloatAttribute<Vector3f> VECTOR_3_F_VERTEX_ATTRIBUTE = new VertexFloatAttribute<>(Vector3f.class, new VertexFloatAttribute.AttributeConfiguration<Vector3f>() {

        @Override
        public void map(Vector3f value, int vertIdx, int stride, int offset, ByteBuffer buffer, Optional<float[]> store) {
            int bufferStart = vertIdx * stride + offset;
            buffer.putFloat(bufferStart, value.x());
            buffer.putFloat(bufferStart + Float.BYTES, value.y());
            buffer.putFloat(bufferStart + Float.BYTES * 2, value.z());

            if(store.isPresent()) {
                float[] data  = store.get();
                data[vertIdx * 3] = value.x();
                data[vertIdx * 3 + 1] = value.y();
                data[vertIdx * 3 + 2] = value.z();
            }

        }

        @Override
        public void map(int pos, float[] value, int vertIdx, int stride, int offset, ByteBuffer buffer,
                        Optional<float[]> store) {
            int bufferStart = vertIdx * stride + offset;
            buffer.putFloat(bufferStart, value[pos]);
            buffer.putFloat(bufferStart + Float.BYTES, value[pos + 1]);
            buffer.putFloat(bufferStart + Float.BYTES * 2, value[pos + 2]);
            if(store.isPresent()) {
                float[] data  = store.get();
                data[vertIdx * 3] = value[pos];
                data[vertIdx * 3 + 1] = value[pos + 1];
                data[vertIdx * 3 + 2] = value[pos + 2];
            }
        }
    }, TypeMapping.ATTR_FLOAT, 3);

    public static final VertexFloatAttribute<Vector4f> VECTOR_4_F_VERTEX_ATTRIBUTE = new VertexFloatAttribute<>(Vector4f.class, new VertexFloatAttribute.AttributeConfiguration<Vector4f>() {
        @Override
        public void map(Vector4f value, int vertIdx, int stride, int offset, ByteBuffer buffer, Optional<float[]> store) {
            int bufferStart = vertIdx * stride + offset;
            buffer.putFloat(bufferStart, value.x());
            buffer.putFloat(bufferStart + Float.BYTES, value.y());
            buffer.putFloat(bufferStart + Float.BYTES * 2, value.z());
            buffer.putFloat(bufferStart + Float.BYTES * 3, value.w());

            if(store.isPresent()) {
                float[] data  = store.get();
                data[vertIdx * 4] = value.x();
                data[vertIdx * 4 + 1] = value.y();
                data[vertIdx * 4 + 2] = value.z();
                data[vertIdx * 4 + 3] = value.w();
            }

        }

        @Override
        public void map(int pos, float[] value, int vertIdx, int stride, int offset, ByteBuffer buffer,
                        Optional<float[]> store) {
            int bufferStart = vertIdx * stride + offset;
            buffer.putFloat(bufferStart, value[pos]);
            buffer.putFloat(bufferStart + Float.BYTES, value[pos + 1]);
            buffer.putFloat(bufferStart + Float.BYTES * 2, value[pos + 2]);
            buffer.putFloat(bufferStart + Float.BYTES * 3, value[pos + 3]);

            if(store.isPresent()) {
                float[] data  = store.get();
                data[vertIdx * 4] = value[pos];
                data[vertIdx * 4 + 1] = value[pos + 1];
                data[vertIdx * 4 + 2] = value[pos + 2];
                data[vertIdx * 4 + 3] = value[pos + 3];
            }
        }
    }, TypeMapping.ATTR_FLOAT, 4);

    public static final VertexFloatAttribute<Color> COLOR_4_F_VERTEX_ATTRIBUTE = new VertexFloatAttribute<>(Color.class, new VertexFloatAttribute.AttributeConfiguration<Color>() {
        @Override
        public void map(Color value, int vertIdx, int stride, int offset, ByteBuffer buffer, Optional<float[]> store) {
            int bufferStart = vertIdx * stride + offset;
            buffer.putFloat(bufferStart, value.rf());
            buffer.putFloat(bufferStart + Float.BYTES, value.gf());
            buffer.putFloat(bufferStart + Float.BYTES * 2, value.bf());
            buffer.putFloat(bufferStart + Float.BYTES * 3, value.af());

            if(store.isPresent()) {
                float[] data  = store.get();
                data[vertIdx * 4] = value.rf();
                data[vertIdx * 4 + 1] = value.gf();
                data[vertIdx * 4 + 2] = value.bf();
                data[vertIdx * 4 + 3] = value.af();
            }
        }

        @Override
        public void map(int pos, float[] value, int vertIdx, int stride, int offset, ByteBuffer buffer,
                        Optional<float[]> store) {
            int bufferStart = vertIdx * stride + offset;
            buffer.putFloat(bufferStart, value[pos]);
            buffer.putFloat(bufferStart + Float.BYTES, value[pos + 1]);
            buffer.putFloat(bufferStart + Float.BYTES * 2, value[pos + 2]);
            buffer.putFloat(bufferStart + Float.BYTES * 3, value[pos + 3]);

            if(store.isPresent()) {
                float[] data  = store.get();
                data[vertIdx * 4] = value[pos];
                data[vertIdx * 4 + 1] = value[pos + 1];
                data[vertIdx * 4 + 2] = value[pos + 2];
                data[vertIdx * 4 + 3] = value[pos + 3];
            }
        }
    }, TypeMapping.ATTR_FLOAT, 4);

    public static final VertexFloatAttribute<Vector2f> VECTOR_2_F_VERTEX_ATTRIBUTE = new VertexFloatAttribute<>(Vector2f.class, new VertexFloatAttribute.AttributeConfiguration<Vector2f>() {
        @Override
        public void map(Vector2f value, int vertIdx, int stride, int offset, ByteBuffer buffer, Optional<float[]> store) {
            int bufferStart = vertIdx * stride + offset;
            buffer.putFloat(bufferStart, value.x());
            buffer.putFloat(bufferStart + Float.BYTES, value.y());

            if(store.isPresent()) {
                float[] data  = store.get();
                data[vertIdx * 2] = value.x();
                data[vertIdx * 2 + 1] = value.y();
            }

        }

        @Override
        public void map(int pos, float[] value, int vertIdx, int stride, int offset, ByteBuffer buffer,
                        Optional<float[]> store) {
            int bufferStart = vertIdx * stride + offset;
            buffer.putFloat(bufferStart, value[pos]);
            buffer.putFloat(bufferStart + Float.BYTES, value[pos + 1]);

            if(store.isPresent()) {
                float[] data  = store.get();
                data[vertIdx * 2] = value[pos];
                data[vertIdx * 2 + 1] = value[pos + 1];
            }
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

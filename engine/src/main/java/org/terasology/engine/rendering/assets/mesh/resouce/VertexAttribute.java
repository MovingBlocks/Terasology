// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.assets.mesh.resouce;

import gnu.trove.list.TFloatList;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TFloatArrayList;
import gnu.trove.list.array.TIntArrayList;
import org.joml.Vector2fc;
import org.joml.Vector3fc;
import org.joml.Vector3ic;
import org.joml.Vector4fc;
import org.lwjgl.opengl.GL30;
import org.terasology.nui.Colorc;

import java.nio.ByteBuffer;
import java.util.Optional;

public class VertexAttribute<TARGET, STORE> {

    public static final VertexAttribute<Vector3ic, TIntList> VECTOR_3_I_VERTEX_ATTRIBUTE = new VertexAttribute<>(
            new AttributeConfiguration<>() {
                @Override
                public void map(int index, ByteBuffer buffer, Vector3ic value) {
                    buffer.putInt(index, value.x());
                    buffer.putInt(index + Integer.BYTES, value.y());
                    buffer.putInt(index + Integer.BYTES * 2, value.z());
                }

                @Override
                public void map(int index, TIntList store, Vector3ic value) {
                    store.set(index, value.x());
                    store.set(index + 1, value.y());
                    store.set(index + 2, value.z());
                }

                @Override
                public void map(int index, TIntList store, int pos, ByteBuffer buffer) {
                    buffer.putInt(pos, store.get(index));
                    buffer.putInt(pos + Integer.BYTES, store.get(index + 1));
                    buffer.putInt(pos + Integer.BYTES * 2, store.get(index + 2));
                }

                @Override
                public TIntList build(int size) {
                    return new TIntArrayList(3 * size);
                }
            }, TypeMapping.ATTR_INT, 3);
    public static final VertexAttribute<Vector3fc, TFloatList> VECTOR_3_F_VERTEX_ATTRIBUTE = new VertexAttribute<>(new AttributeConfiguration<>() {
        @Override
        public void map(int index, ByteBuffer buffer, Vector3fc value) {
            buffer.putFloat(index, value.x());
            buffer.putFloat(index + Float.BYTES, value.y());
            buffer.putFloat(index + Float.BYTES * 2, value.z());
        }

        @Override
        public void map(int index, TFloatList store, Vector3fc value) {
            store.set(index, value.x());
            store.set(index + 1, value.y());
            store.set(index + 2, value.z());
        }

        @Override
        public void map(int index, TFloatList store, int pos, ByteBuffer buffer) {
            buffer.putFloat(pos, store.get(index));
            buffer.putFloat(pos + Float.BYTES, store.get(index + 1));
            buffer.putFloat(pos + Float.BYTES * 2, store.get(index + 2));
        }

        @Override
        public TFloatList build(int size) {
            return new TFloatArrayList(3 * size);
        }
    }, TypeMapping.ATTR_FLOAT, 3);

    public static final VertexAttribute<Vector4fc, TFloatList> VECTOR_4_F_VERTEX_ATTRIBUTE = new VertexAttribute<>(new AttributeConfiguration<>() {
        @Override
        public void map(int index, ByteBuffer buffer, Vector4fc value) {
            buffer.putFloat(index, value.x());
            buffer.putFloat(index + Float.BYTES, value.y());
            buffer.putFloat(index + Float.BYTES * 2, value.z());
            buffer.putFloat(index + Float.BYTES * 3, value.z());
        }

        @Override
        public void map(int index, TFloatList store, Vector4fc value) {
            store.set(index, value.x());
            store.set(index + 1, value.y());
            store.set(index + 2, value.z());
            store.set(index + 3, value.w());
        }

        @Override
        public void map(int index, TFloatList store, int pos, ByteBuffer buffer) {
            buffer.putFloat(pos, store.get(index));
            buffer.putFloat(pos + Float.BYTES, store.get(index + 1));
            buffer.putFloat(pos + Float.BYTES * 2, store.get(index + 2));
            buffer.putFloat(pos + Float.BYTES * 3, store.get(index + 3));
        }

        @Override
        public TFloatList build(int size) {
            return new TFloatArrayList(4 * size);
        }
    }, TypeMapping.ATTR_FLOAT, 4);

    public static final VertexAttribute<Colorc, TFloatList> COLOR_4_F_VERTEX_ATTRIBUTE = new VertexAttribute<>(new AttributeConfiguration<>() {
        @Override
        public void map(int index, ByteBuffer buffer, Colorc value) {
            buffer.putFloat(index, value.rf());
            buffer.putFloat(index + Float.BYTES, value.gf());
            buffer.putFloat(index + Float.BYTES * 2, value.bf());
            buffer.putFloat(index + Float.BYTES * 3, value.af());
        }

        @Override
        public void map(int index, TFloatList store, Colorc value) {
            store.set(index, value.rf());
            store.set(index + 1, value.gf());
            store.set(index + 2, value.bf());
            store.set(index + 3, value.af());
        }

        @Override
        public void map(int index, TFloatList store, int pos, ByteBuffer buffer) {
            buffer.putFloat(pos, store.get(index));
            buffer.putFloat(pos + Float.BYTES, store.get(index + 1));
            buffer.putFloat(pos + Float.BYTES * 2, store.get(index + 2));
        }

        @Override
        public TFloatList build(int size) {
            return new TFloatArrayList(4 * size);
        }
    }, TypeMapping.ATTR_FLOAT, 4);

    public static final VertexAttribute<Vector2fc, TFloatList> VECTOR_2_F_VERTEX_ATTRIBUTE = new VertexAttribute<>(new AttributeConfiguration<>() {

        @Override
        public void map(int index, int stride, int offset, Optional<TFloatList> data, ByteBuffer buffer, Vector2fc value) {
            int start = index * stride + offset;
            buffer.putFloat(start, value.x());
            buffer.putFloat(start + Float.BYTES, value.x());

            data.ifPresent(k -> {
                k.set(index, value.x());
                k.set(index + 1, value.y());
            });
        }

        @Override
        public void map(int index, int stride, int offset, Optional<TFloatList> data, ByteBuffer buffer, int index1,
                        TFloatList value) {

            int start = index * stride + offset;
            buffer.putFloat(start, value.get(index1));
            buffer.putFloat(start + Float.BYTES, value.get(index1 + 1));

            data.ifPresent(k -> {
                k.set(index, value.get(index1));
                k.set(index + 1, value.get(index1 + 1));
            });
        }

        @Override
        public TFloatList build(int size) {
            return new TFloatArrayList(2 * size);
        }
    }, TypeMapping.ATTR_FLOAT, 2);

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

    interface AttributeConfiguration<TARGET, STORE> {
        void map(int index, int stride, int offset, Optional<STORE> data, ByteBuffer buffer, TARGET value);

        void map(int index, int stride, int offset, Optional<STORE> data, ByteBuffer buffer, int index1, STORE value);


        STORE build(int size);
    }

    public final AttributeConfiguration<TARGET, STORE> configuration;
    public final TypeMapping mapping;
    public final int count;

    private VertexAttribute(AttributeConfiguration<TARGET, STORE> configuration, TypeMapping mapping, int count) {
        this.configuration = configuration;
        this.mapping = mapping;
        this.count = count;
    }
}

// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.assets.mesh.resouce;

import java.lang.reflect.Array;
import java.nio.ByteBuffer;

public class VertexFloatAttribute<TARGET> extends VertexAttribute<TARGET> {

    public static VertexAttributeFloatBinding EMPTY_BINDING = new VertexAttributeFloatBinding(null, VECTOR_3_F_VERTEX_ATTRIBUTE, 0, 0, false);

    public interface AttributeConfiguration<TARGET> {
        void map(TARGET value, int vertIdx, int stride, int offset, ByteBuffer buffer);

        void map(int pos, float[] value, int vertIdx, int stride, int offset, ByteBuffer buffer);

        void map(TARGET value, int vertIdx, TARGET[] store);

        void map(int pos, float[] value, int vertIdx, TARGET[] store);

        TARGET build();
    }
    public final VertexFloatAttribute.AttributeConfiguration<TARGET> configuration;
    protected VertexFloatAttribute(Class<TARGET> type,VertexFloatAttribute.AttributeConfiguration<TARGET> configuration,
                                VertexAttribute.TypeMapping mapping, int count) {
        super(type, mapping, count);
        this.configuration = configuration;
    }

    public static class VertexAttributeFloatBinding<TARGET> extends VertexAttributeBinding<TARGET> {
        private VertexFloatAttribute<TARGET> attribute;
        private int index = 0;
        private final int offset;
        private int elements;
        private TARGET[] store = null;

        protected VertexAttributeFloatBinding(VertexResource resource, VertexFloatAttribute<TARGET> target, int offset, int elements, boolean cpuReadable) {
            super(resource);
            this.attribute = target;
            this.offset = offset;
            this.elements = elements;
            if (cpuReadable) {
                store = (TARGET[]) Array.newInstance(target.type, this.elements);
                for (int x = 0; x < this.elements; x++) {
                    store[x] = target.configuration.build();
                }
            }
        }

        @Override
        public TARGET[] getStore() {
            return store;
        }

        @Override
        public int count() {
            return this.elements;
        }

        @Override
        public void rewind() {
            index = 0;
        }

        private void update() {
            if (this.resource.elements() != this.elements && this.store != null) {
                TARGET[] newStore = (TARGET[]) Array.newInstance(this.attribute.type, this.elements);
                int size = Math.min(newStore.length, this.store.length);
                System.arraycopy(store, 0, newStore, 0, size);
                if (newStore.length > this.store.length) {
                    for (int x = this.store.length; x < newStore.length; x++) {
                        newStore[x] = attribute.configuration.build();
                    }
                }
                this.store = newStore;
                this.elements = this.resource.elements();
            }
        }

        @Override
        public void map(int startIndex, int endIndex, float[] arr, int offsetIndex) {
            if(startIndex == endIndex)
                return;

            int posIndex = 0;
            update();
            for (int x = startIndex; x < endIndex; x++) {
                if (store != null) {
                    attribute.configuration.map(x * attribute.count, arr, offsetIndex + posIndex, store);
                }
                attribute.configuration.map(x * attribute.count, arr, offsetIndex + posIndex, resource.inStride, offset, resource.buffer);
                posIndex++;
            }
            resource.mark();
        }

        @Override
        public void put(int vertexIndex, TARGET value) {
            attribute.configuration.map(value, vertexIndex, resource.inStride, offset, resource.buffer);
            update();
            if (store != null) {
                attribute.configuration.map(value, vertexIndex, store);
            }
            resource.mark();

        }

        @Override
        public void put(TARGET value) {
            attribute.configuration.map(value, index, resource.inStride, offset, resource.buffer);
            update();
            if (store != null) {
                attribute.configuration.map(value, index, store);
            }
            resource.mark();
            index++;
        }

        @Override
        public void refresh() {
            if (store != null) {
                update();
                for (int x = 0; x < this.elements; x++) {
                    attribute.configuration.map(store[x], x, resource.inStride, offset, resource.buffer);
                }
                resource.mark();
            }
        }
    }
}

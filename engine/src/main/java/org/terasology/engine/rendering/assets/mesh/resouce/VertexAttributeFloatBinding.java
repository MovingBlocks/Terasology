// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.assets.mesh.resouce;

import gnu.trove.list.array.TFloatArrayList;

import java.util.Optional;

/**
 * maps a floating point arrays to a target buffer.
 *
 * @param <TARGET> object that backs the buffer
 */
public class VertexAttributeFloatBinding<TARGET> extends VertexAttributeBinding<TARGET> {
    private VertexFloatAttribute<TARGET> attribute;
    private int index = 0;
    private final int offset;
    private float[] store = new float[]{};

    public VertexAttributeFloatBinding(VertexResource resource, VertexFloatAttribute<TARGET> target, int offset) {
        super(resource);
        this.attribute = target;
        this.offset = offset;
    }

    public void ensureCapacity(int elements) {
        // assumption that the data is packed by stride
        resource.ensureCapacity(resource.getInStride() * elements);
        int newCap = Math.max(store.length << 1, elements * attribute.count);
        float[] tmp = new float[newCap];
        System.arraycopy(store, 0, tmp, 0, store.length);
        store = tmp;
    }

    public float get(int index) {
        return store[index];
    }

    public int length() {
        return store.length;
    }

    public int elements() {
        return store.length / attribute.count;
    }

    public float[] getStore() {
        return store;
    }

    @Override
    public void rewind() {
        index = 0;
    }

    @Override
    public void map(int startIndex, int endIndex, float[] arr, int offsetIndex) {
        if (startIndex == endIndex)
            return;
        int posIndex = 0;
        ensureCapacity((endIndex - startIndex) + offsetIndex);
        for (int x = startIndex; x < endIndex; x++) {
            attribute.configuration.map(x * attribute.count, arr, offsetIndex + posIndex, resource.getInStride(), offset, resource.buffer, Optional.ofNullable(this.store));
            posIndex++;
        }
        resource.mark();
    }

    @Override
    public void put(int vertexIndex, TARGET value) {
        ensureCapacity(vertexIndex + 1);
        attribute.configuration.map(value, vertexIndex, resource.getInStride(), offset, resource.buffer, Optional.ofNullable(this.store));
        resource.mark();

    }

    @Override
    public void put(TARGET value) {
        ensureCapacity(index + 1);
        attribute.configuration.map(value, index, resource.getInStride(), offset, resource.buffer, Optional.ofNullable(this.store));
        resource.mark();
        index++;
    }

    @Override
    public void commit() {
        if (store != null) {
            for (int index = 0; index < this.elements(); index++) {
                attribute.configuration.map(index * attribute.count, store, index, resource.getInStride(), offset, resource.buffer, Optional.empty());
            }
            resource.mark();
        }
    }
}

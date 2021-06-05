// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.assets.mesh.resource;

/**
 * defines the order of vertices to walk for rendering geometry
 *
 * refrence: https://www.khronos.org/opengl/wiki/Primitive
 */
public class IndexResource extends BufferedResource {
    private int inIndices = 0;
    private int posIndex = 0;

    public int indices() {
        return inIndices;
    }

    public IndexResource() {
        super();
    }


    public void copy(IndexResource resource) {
        copyBuffer(resource);
        this.inSize = resource.inSize;
        this.inIndices = resource.indices();
    }


    public void reserveElements(int elements) {
        reserve(elements * Integer.BYTES);
    }

    public void rewind() {
        posIndex = 0;
    }

    public void put(int value) {
        if (posIndex >= inIndices) {
            inIndices++;
            allocate(inIndices * Integer.BYTES);
        }
        buffer.putInt(posIndex * Integer.BYTES, value);
        posIndex++;
    }

    public void putAll(int value, int ... values) {
        put(value);
        for (int x = 0; x < values.length; x++) {
            put(values[x]);
        }
    }


    public void putAll(int[] values) {
        for (int x = 0; x < values.length; x++) {
            put(values[x]);
        }
    }

    public void allocateElements(int indices) {
        allocate(indices * Integer.BYTES);
        inIndices = indices;
    }

    public void position(int position) {
        posIndex = position;
    }

    public void put(int index, int value) {
        buffer.putInt(index * Integer.BYTES, value);
    }

    @Override
    public boolean isEmpty() {
        return inIndices == 0;
    }




}

// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.assets.mesh.resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * defines the order of vertices to walk for rendering geometry
 *
 * refrence: https://www.khronos.org/opengl/wiki/Primitive
 */
public class IndexResource extends BufferedResource {
    public static final Logger logger = LoggerFactory.getLogger(IndexResource.class);
    private int inIndices = 0;
    private int posIndex = 0;

    public IndexResource() {
        super();
    }

    public int indices() {
        return inIndices;
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
        ensureCapacity((posIndex + 1) * Integer.BYTES);
        buffer.putInt(posIndex * Integer.BYTES, value);
        posIndex++;
        if (posIndex > inIndices) {
            inIndices = posIndex;
        }
    }

    public void putAll(int value, int... values) {
        put(value);
        for (int i : values) {
            put(i);
        }
    }


    public void putAll(int[] values) {
        for (int value : values) {
            put(value);
        }
    }

    public void allocateElements(int indices) {
        int size = indices * Integer.BYTES;
        allocate(size);
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

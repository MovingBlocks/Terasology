// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.assets.mesh.resource;

import org.lwjgl.BufferUtils;
import java.nio.ByteBuffer;

public class IndexResource {
    public ByteBuffer buffer;
    private int numIndices = 0;
    private int inSize = 0;
    private int posIndex = 0;


    public int getNumberOfIndices() {
        return numIndices;
    }

    public int getSize() {
        return numIndices * Integer.BYTES;
    }

    public IndexResource() {
        this.buffer = BufferUtils.createByteBuffer(0);
    }

    public void ensureCapacity(int size) {
        if (size > buffer.capacity()) {
            int newCap = Math.max(this.inSize << 1, size);
            ByteBuffer newBuffer = BufferUtils.createByteBuffer(newCap);
            buffer.limit(Math.min(size, inSize));
            buffer.position(0);
            newBuffer.put(buffer);
            this.buffer = newBuffer;
        }
        if (size > this.inSize) {
            this.inSize = size;
        }
    }

    public void rewind() {
        posIndex = 0;
    }

    public void put(int value) {
        ensureCapacity((posIndex + 1) * Integer.BYTES);
        buffer.putInt(posIndex * Integer.BYTES, value);
        posIndex++;
        if (posIndex > numIndices) {
            numIndices = posIndex;
        }
    }

    public void put(int index, int value) {
        buffer.putInt(index * Integer.BYTES, value);
    }
}

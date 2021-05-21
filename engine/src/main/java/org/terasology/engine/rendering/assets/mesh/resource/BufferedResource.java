// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.assets.mesh.resource;

import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;

public class BufferedResource {

    protected int inSize = 0;
    protected ByteBuffer buffer = BufferUtils.createByteBuffer(0);

    public ByteBuffer buffer() {
        return this.buffer;
    }

    public int inSize() {
        return inSize;
    }

    public boolean isEmpty() {
        return inSize == 0;
    }

    public void copyBuffer(BufferedResource resource) {
        ensureCapacity(resource.inSize);
        ByteBuffer copyBuffer = resource.buffer;
        copyBuffer.limit(resource.inSize());
        copyBuffer.rewind();
        buffer.put(copyBuffer);

        this.inSize = resource.inSize;
    }

    public void ensureCapacity(int size) {
        if (size > buffer.capacity()) {
            int newCap = Math.max(this.inSize << 1, size);
            ByteBuffer newBuffer = BufferUtils.createByteBuffer(newCap);
            buffer.limit(Math.min(size, this.inSize));
            buffer.position(0);
            newBuffer.put(buffer);
            this.buffer = newBuffer;
        }
        if (size > this.inSize) {
            this.inSize = size;
        }
    }
}

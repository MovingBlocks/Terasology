// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.assets.mesh.resource;

import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

public abstract class BufferedResource {

    protected int inSize = 0;
    protected ByteBuffer buffer = BufferUtils.createByteBuffer(0);

    ByteBuffer buffer() {
        return this.buffer;
    }


    public abstract boolean isEmpty();

    public int inSize() {
        return inSize;
    }

    public void copyBuffer(ByteBuffer copyBuffer) {
        ensureCapacity(copyBuffer.capacity());
        buffer.put(copyBuffer);
    }

    public void copyBuffer(BufferedResource resource) {
        ensureCapacity(resource.inSize);
        ByteBuffer copyBuffer = resource.buffer;
        copyBuffer.limit(resource.inSize);
        copyBuffer.rewind();
        buffer.put(copyBuffer);

        this.inSize = resource.inSize;
    }

    protected void reserve(int size) {
        if (size > buffer.capacity()) {
            int newCap = Math.max(buffer.capacity() << 1, size);
            ByteBuffer newBuffer = BufferUtils.createByteBuffer(newCap);
            buffer.limit(this.inSize);
            buffer.position(0);
            newBuffer.put(buffer);
            this.buffer = newBuffer;
        }
    }

    protected void allocate(int size) {
        ensureCapacity(size);
        this.inSize = size;
    }

    protected void ensureCapacity(int size) {
        if (size > buffer.capacity()) {
            int newCap = Math.max(this.buffer.capacity() << 1, size);
            ByteBuffer newBuffer = BufferUtils.createByteBuffer(newCap);
            buffer.limit(this.inSize);
            buffer.position(0);
            newBuffer.put(buffer);
            this.buffer = newBuffer;
        }
        if (size > this.inSize) {
            this.inSize = size;
        }
    }

    public void writeBuffer(Consumer<ByteBuffer> consumer) {
        buffer.rewind();
        buffer.limit(inSize);
        consumer.accept(buffer);
    }

    public void squeeze() {
        if (this.inSize != buffer.capacity()) {
            ByteBuffer newBuffer = BufferUtils.createByteBuffer(this.inSize);
            buffer.limit(this.inSize);
            buffer.position(0);
            newBuffer.put(buffer);
            this.buffer = newBuffer;
        }
    }
}

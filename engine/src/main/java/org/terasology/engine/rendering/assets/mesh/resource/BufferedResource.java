// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.assets.mesh.resource;

import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

/**
 * buffered resource is resource for graphics resource.
 *
 * Access to a DirectBuffer is close to equivelent to accessing an array so managing it in this
 * form is more optimal when transferring to the hardware.
 *
 * Used for managing data for vertex and index data
 */
public abstract class BufferedResource {

    protected int inSize = 0;
    protected ByteBuffer buffer = BufferUtils.createByteBuffer(0);

    ByteBuffer buffer() {
        return this.buffer;
    }

    /**
     * the size of the buffer allocated but the capacity can be larger to account for growth.
     * @return the size of the active buffer
     */
    public int inSize() {
        return inSize;
    }

    /**
     * determines if the buffer is empty
     * @return an empty buffer
     */
    public abstract boolean isEmpty();

    /**
     * copy the contents of another buffer direction into this resource.
     *
     * make sure the data is structured in a way that the buffer expects.
     * @param copyBuffer the buffer to copy
     */
    public void copyBuffer(ByteBuffer copyBuffer) {
        allocate(copyBuffer.capacity());
        buffer.put(copyBuffer);
    }

    /**
     * copy the buffer from another resource to this one
     * @param resource the expected resource
     */
    public void copyBuffer(BufferedResource resource) {
        ensureCapacity(resource.inSize);
        ByteBuffer copyBuffer = resource.buffer;
        copyBuffer.limit(resource.inSize);
        copyBuffer.rewind();
        buffer.put(copyBuffer);

        this.inSize = resource.inSize;
    }

    /**
     * expand the capacity of the buffer without increasing {@link #inSize()}
     * @param size the size of the capacity of the buffer
     */
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

    /**
     * allocate the buffer to match {@link #inSize()}
     * @param size the size the buffer should be allocated to
     */
    protected void allocate(int size) {
        ensureCapacity(size);
        this.inSize = size;
    }

    /**
     * ensure the buffer is large enough for the size requested. {@link #inSize()} will
     * use the current size if the buffer is already larger then the requested size else
     * the size is set to the requested
     * @param size the size of the buffer
     */
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

    /**
     * write the results of the buffer to a consumer.
     * the buffer is rewinded to the back and the limit is set to the expected {@link #inSize()}
     * @param consumer
     */
    public void writeBuffer(Consumer<ByteBuffer> consumer) {
        buffer.rewind();
        buffer.limit(inSize);
        consumer.accept(buffer);
    }

    /**
     * shrink the buffer capacity to match the {@link #inSize()}
     */
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

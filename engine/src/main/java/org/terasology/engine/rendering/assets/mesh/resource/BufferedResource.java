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
    private short version;

    /**
     * increase version flag for change
     */
    public void mark() {
        this.version++;
    }

    /**
     * the version of the buffer is used to determine if the contents has changed. this should notify the end user of the buffer to sync the
     * data back to the driver
     *
     * @return the version flag
     */
    public int getVersion() {
        return this.version;
    }

    ByteBuffer buffer() {
        return this.buffer;
    }

    /**
     * the size of the buffer allocated but the capacity can be larger to account for growth.
     *
     * @return the size of the active buffer
     */
    public int inSize() {
        return inSize;
    }

    /**
     * determines if the buffer is empty
     *
     * @return an empty buffer
     */
    public abstract boolean isEmpty();

    /**
     * append the buffer to the current BufferedResource
     * <p>
     * make sure the data is structured in a way that the buffer expects.
     *
     * All {@link BufferedResource} are all in native order. data will
     * be garbage if the order of the buffer is different from the endianness system.
     *
     * @param copyBuffer the buffer to replace the contents with
     */
    public void put(ByteBuffer copyBuffer) {
        // ensure the buffer has the correct capacity
        reserve(this.inSize + copyBuffer.limit());

        // rewind buffer
        copyBuffer.rewind();

        buffer.position(this.inSize);
        buffer.put(copyBuffer);

        this.inSize += copyBuffer.limit();
        mark();
    }

    /**
     * append the buffer to the current BufferedResource
     *
     * <p>
     * make sure the data is structured in a way that the buffer expects.
     *
     * @param resource the resource to append the contents with
     */
    public void put(BufferedResource resource) {
        // ensure the buffer has the correct capacity
        reserve(this.inSize + resource.inSize);

        ByteBuffer copyBuffer = resource.buffer;
        copyBuffer.limit(resource.inSize);
        copyBuffer.rewind();

        buffer.position(this.inSize);
        buffer.put(copyBuffer);
        this.inSize += resource.inSize;
        mark();
    }


    /**
     * replace this resource directory with the contents from another buffer
     * <p>
     * make sure the data is structured in a way that the buffer expects.
     *
     * All {@link BufferedResource} are all in native order. data will
     * be garbage if the order of the buffer is different from the endianness system.
     *
     * @param copyBuffer the buffer to replace the contents with
     */
    public void replace(ByteBuffer copyBuffer) {
        reserve(copyBuffer.limit());

        buffer.rewind();
        buffer.put(copyBuffer);

        this.inSize = copyBuffer.limit();
        mark();
    }

    /**
     * replace this with the resource supplied
     *
     * @param resource the resource to replace the contents with
     */
    public void replace(BufferedResource resource) {
        reserve(resource.inSize);

        ByteBuffer copyBuffer = resource.buffer;
        copyBuffer.limit(resource.inSize);
        copyBuffer.rewind();

        buffer.rewind();
        buffer.put(copyBuffer);

        this.inSize = resource.inSize;
        mark();
    }

    /**
     * expand the capacity of the buffer without increasing {@link #inSize()}
     *
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
        mark();
    }

    /**
     * allocate the buffer to match {@link #inSize()}
     *
     * @param size the size the buffer should be allocated to
     */
    protected void allocate(int size) {
        ensureCapacity(size);
        this.inSize = size;
        mark();
    }

    /**
     * ensure the buffer is large enough for the size requested. {@link #inSize()} will use the current size if the buffer is already larger
     * then the requested size else the size is set to the requested
     *
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
        buffer.limit(inSize);
        mark();
    }

    /**
     * write the results of the buffer to a consumer. the buffer is rewinded to the back and the limit is set to the expected {@link
     * #inSize()}
     *
     * @param consumer
     */
    public void writeBuffer(Consumer<ByteBuffer> consumer) {
        buffer.rewind();
        buffer.limit(inSize);
        consumer.accept(buffer);
    }

    /**
     * shrink the buffer capacity to match {@link #inSize()}
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

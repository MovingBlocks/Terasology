// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.assets.mesh.resource;

import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;

public class VertexResource extends BufferedResource {
    private int inStride = 0;
    private int version = 0;
    private VertexDefinition[] attributes;

    public int inSize() {
        return inSize;
    }

    public VertexResource() {

    }

    public VertexDefinition[] definitions() {
        return this.attributes;
    }


    public void setDefinitions(VertexDefinition[] attr) {
        this.attributes = attr;
    }

    public VertexResource(int inSize, int inStride, VertexDefinition[] attributes) {
        ensureCapacity(inSize);
        this.inStride = inStride;
        this.inSize = inSize;
        this.attributes = attributes;
    }

    public void copy(VertexResource resource) {
        if (resource.isEmpty()) {
            return;
        }
        copyBuffer(resource);
        this.inStride = resource.inStride;
        this.mark();
    }

    public int inStride() {
        return inStride;
    }

    public void reserveElements(int verts) {
        int size = verts * inStride;
        ensureCapacity(size);
    }

    public void reallocateElements(int verts) {
        int size = verts * inStride;
        ensureCapacity(size);
        this.inSize = size;
        squeeze();

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

    public void allocate(int size, int stride) {
        this.ensureCapacity(size);
        this.inStride = stride;
        this.inSize = size;
    }

    public int getVersion() {
        return version;
    }

    /**
     * increase version flag for change
     */
    public void mark() {
        version++;
    }

    /**
     * describes the metadata and placement into the buffer based off the stride.
     */
    public static class VertexDefinition {
        public final int location;
        public final VertexAttribute attribute;
        public final int offset;

        public VertexDefinition(int location, int offset, VertexAttribute attribute) {
            this.location = location;
            this.attribute = attribute;
            this.offset = offset;
        }
    }
}

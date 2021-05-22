// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.assets.mesh.resource;

import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;

public class VertexResource extends BufferedResource {
    private int inStride = 0;
    private int version = 0;
    private VertexDefinition[] attributes;


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
        reserve(size);
    }

    public void allocateElements(int verts) {
        int size = verts * inStride;
        allocate(size);
        squeeze();
    }

    public void allocate(int size, int stride) {
        this.allocate(size);
        this.inStride = stride;
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

    @Override
    public boolean isEmpty() {
        return inSize == 0;
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

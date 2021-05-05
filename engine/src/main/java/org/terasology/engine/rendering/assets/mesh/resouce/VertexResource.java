// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.assets.mesh.resouce;

import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class VertexResource {
    private int inStride;
    private int inSize;
    public ByteBuffer buffer;
    private int version = 0;
    public VertexDefinition[] attributes;

    public int inSize() {
        return inSize;
    }

    public VertexResource() {

    }

    public void allocate(int inSize, int inStride) {
        this.inSize = inSize;
        this.inStride = inStride;
        ensureCapacity(inSize);
    }

    public void setDefinitions(VertexDefinition[] attributes) {
        this.attributes = attributes;
    }

    public VertexResource(int inSize, int inStride, VertexDefinition[] attributes) {
        this.inStride = inStride;
        this.inSize = inSize;
        this.attributes = attributes;
        this.buffer = BufferUtils.createByteBuffer(inSize);
    }

    public void reallocate(int inStride, int inSize) {
        this.inStride = inStride;
        this.inSize = inSize;
    }

    public int getInSize() {
        return inSize;
    }

    public int getInStride() {
        return inStride;
    }

    public void ensureCapacity(int inSize) {
        if(inSize > buffer.capacity()) {
            int newCap = Math.max(inSize << 1, inSize);
            ByteBuffer newBuffer = BufferUtils.createByteBuffer(newCap);
            buffer.limit(Math.min(inSize, this.inSize));
            buffer.position(0);
            newBuffer.put(buffer);
            this.buffer = newBuffer;
            this.inSize = inSize;
        }
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
        public final int stride;

        public VertexDefinition(int location, int stride, VertexAttribute attribute) {
            this.location = location;
            this.attribute = attribute;
            this.stride = stride;
        }
    }

    /**
     * Builder that that creates bindings for {@link VertexResource}
     */
    public static class VertexResourceBuilder {
        private List<VertexDefinition> definitions = new ArrayList<>();
        private int inStride;
        private final int elements;
        private VertexResource resource = new VertexResource();

        public VertexResourceBuilder(int elements) {
            this.elements = elements;
        }

        /**
         * add an attribute and provides an {@link VertexAttributeFloatBinding}
         *
         * @param location the index of the attribute binding
         * @param attribute the attribute that describes the binding
         * @param cpuReadable exposes the data to a {@link TARGET} array
         * @param <TARGET>
         * @return
         */
        public <TARGET> VertexAttributeFloatBinding<TARGET> add(int location,
                                                                                     VertexFloatAttribute<TARGET> attribute, boolean cpuReadable) {
            VertexAttributeFloatBinding<TARGET> result =
                    new VertexAttributeFloatBinding<>(resource,attribute, inStride);
            this.definitions.add(new VertexDefinition(location, inStride, attribute));
            inStride += attribute.mapping.size * attribute.count;
            return result;
        }

        public VertexResource build() {
            resource.allocate(this.inStride * this.elements, this.inStride);
            resource.setDefinitions(definitions.toArray(new VertexDefinition[]{}));
            return resource;
        }
    }
}

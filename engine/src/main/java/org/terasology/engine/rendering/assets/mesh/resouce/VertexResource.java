// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.assets.mesh.resouce;

import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class VertexResource {
    public int inStride;
    public VertexDefinition[] attributes;
    public ByteBuffer buffer;
    private int version = 0;
    private int elements = 0;

    protected VertexResource(int elements, int inStride, VertexDefinition[] attribute) {
        this.inStride = inStride;
        this.elements = elements;
        this.buffer = BufferUtils.createByteBuffer(elements * inStride);
        this.attributes = attribute;
    }

    public int elements() {
        return elements;
    }
    public int inSize() {
        return elements * inStride;
    }

    protected VertexResource() {
        this.elements = 0;
        this.inStride = 0;
        attributes = new VertexDefinition[]{};
        buffer = BufferUtils.createByteBuffer(0);
    }

    public void reallocate(int elements, int inStride, VertexDefinition[] attributes) {
        ByteBuffer newBuffer = BufferUtils.createByteBuffer(elements * inStride);
        this.buffer.limit(Math.min(elements * inStride, this.elements * this.inStride));
        this.buffer.position(0);
        newBuffer.put(this.buffer);

        this.buffer = newBuffer;
        this.elements = elements;
        this.attributes = attributes;
        this.inStride = inStride;
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


    public static class VertexResourceBuilder {
        private List<VertexDefinition> definitions = new ArrayList<>();
        private int inStride;
        private int elements;
        private VertexResource vertexResource = new VertexResource();

        public VertexResourceBuilder(int elements) {
            this.elements = elements;
        }

        public <TARGET> VertexFloatAttribute.VertexAttributeFloatBinding<TARGET> add(int location,
                                                                                     VertexFloatAttribute<TARGET> attribute, boolean cpuReadable) {
            VertexFloatAttribute.VertexAttributeFloatBinding<TARGET> result =
                    new VertexFloatAttribute.VertexAttributeFloatBinding<>(vertexResource, attribute, inStride, elements,
                            cpuReadable);
            this.definitions.add(new VertexDefinition(location, inStride, attribute));
            inStride += attribute.mapping.size * attribute.count;
            return result;
        }

        public VertexResource build() {
            vertexResource.reallocate(this.elements, this.inStride, this.definitions.toArray(new VertexDefinition[]{}));
            return vertexResource;
        }
    }
}

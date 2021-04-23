// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.assets.mesh.resouce;

import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class VertexResource {
    public int inStride;
    public int inSize;
    public VertexDefinition[] attributes;
    public ByteBuffer buffer;
    private int version = 0;

    protected VertexResource(int inStride, int inSize, VertexDefinition[] attribute) {
        this.inSize = inSize;
        this.inStride = inStride;
        this.buffer = BufferUtils.createByteBuffer(inSize);
        this.attributes = attribute;
    }

    protected VertexResource() {
        this.inSize = 0;
        this.inStride = 0;
        attributes = new VertexDefinition[]{};
        buffer = BufferUtils.createByteBuffer(0);
    }

    public void allocate(int inStride, int inSize, VertexDefinition[] attributes) {
        this.attributes = attributes;
        this.inStride = inStride;
        this.inSize = inSize;
        this.buffer = BufferUtils.createByteBuffer(this.inSize);
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
        private List<VertexAttributeBinding> bindings = new ArrayList<>();
        private int inStride;
        private int vertexCount;
        private VertexResource vertexResource = new VertexResource();


        public VertexResourceBuilder(int vertexCount) {
            this.vertexCount = vertexCount;
        }

        public <TARGET> VertexFloatAttribute.VertexAttributeFloatBinding<TARGET> add(int location,
                                                                                     VertexFloatAttribute<TARGET> attribute, boolean cpuReadable) {
            VertexFloatAttribute.VertexAttributeFloatBinding<TARGET> result =
                    new VertexFloatAttribute.VertexAttributeFloatBinding<>(vertexResource, attribute, inStride, vertexCount,
                            cpuReadable);
            this.bindings.add(result);
            this.definitions.add(new VertexDefinition(location, inStride, attribute));
            inStride += attribute.mapping.size * attribute.count;
            return result;
        }

        public VertexResource build() {
            vertexResource.allocate(inStride, inStride * vertexCount, this.definitions.toArray(new VertexDefinition[]{}));
            return vertexResource;
        }
    }
}

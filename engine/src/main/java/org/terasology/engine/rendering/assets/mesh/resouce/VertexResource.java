// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.assets.mesh.resouce;

import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class VertexResource {
    public final int inStride;
    public final int inSize;
    public final VertexDefinition[] attributes;
    public final ByteBuffer buffer;
    private int version = 0;

    protected VertexResource(int inStride, int inSize, VertexDefinition[] attribute) {
        this.inSize = inSize;
        this.inStride = inStride;
        this.buffer = BufferUtils.createByteBuffer(inSize);
        this.attributes = attribute;
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

        public VertexDefinition(int location, VertexAttribute attribute) {
            this.location = location;
            this.attribute = attribute;
        }
    }


    public static class VertexResourceBuilder {
        private List<VertexDefinition> definitions = new ArrayList<>();
        private List<VertexAttributeBinding> bindings = new ArrayList<>();
        private int inStride;
        private int vertexCount;

        public VertexResourceBuilder(int vertexCount) {
            this.vertexCount = vertexCount;
        }


        public <TARGET> VertexFloatAttribute.VertexAttributeFloatBinding<TARGET> add(int location,
                                                                                     VertexFloatAttribute<TARGET> attribute, boolean cpuReadable) {
            VertexFloatAttribute.VertexAttributeFloatBinding<TARGET> result =
                    new VertexFloatAttribute.VertexAttributeFloatBinding<>(attribute, inStride, vertexCount,
                            cpuReadable);
            inStride += attribute.mapping.size * attribute.count;
            this.bindings.add(result);
            this.definitions.add(new VertexDefinition(location, attribute));
            return result;
        }

        public VertexResource build() {
            VertexResource resource = new VertexResource(inStride, inStride * vertexCount,
                    this.definitions.toArray(new VertexDefinition[]{}));
            for (VertexAttributeBinding binding : bindings) {
                binding.setResource(resource);
            }
            return resource;
        }
    }
}

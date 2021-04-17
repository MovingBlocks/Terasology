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

    protected VertexResource(int inStride, int inSize, VertexDefinition[] attribute) {
        this.inSize = inSize;
        this.inStride = inStride;
        this.buffer = BufferUtils.createByteBuffer(inSize);
        this.attributes = attribute;
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
        private List<VertexAttributeBinding<?, ?>> bindings = new ArrayList<>();
        private List<VertexDefinition> definitions = new ArrayList<>();
        private int inStride;
        private int vertexCount;

        public VertexResourceBuilder(int vertexCount) {
            this.vertexCount = vertexCount;
        }


        public <TARGET, STORE> VertexAttributeBinding<TARGET, STORE> add(int location, VertexAttribute<TARGET, STORE> attribute, boolean cpuReadable) {
            VertexAttributeBinding<TARGET, STORE> result = new VertexAttributeBinding<TARGET, STORE>(attribute, inStride, vertexCount, cpuReadable);
            inStride += attribute.mapping.size * attribute.count;
            this.bindings.add(result);
            this.definitions.add(new VertexDefinition(location, attribute));
            return result;
        }

        public VertexResource build() {
            VertexResource resource = new VertexResource(inStride, inStride * vertexCount, this.definitions.toArray(new VertexDefinition[]{}));
            for (VertexAttributeBinding binding : bindings) {
                binding.resource = resource;
            }
            return resource;
        }
    }

    public static class VertexAttributeBinding<TARGET, STORE> {
        private VertexAttribute<TARGET, STORE> attributes;
        private VertexResource resource;
        private int index = 0;
        private int offset = 0;
        private int vertexCount;
        private STORE backing;

        public Optional<STORE> getBacking() {
            return Optional.ofNullable(backing);
        }

        public int vertexCount() {
            return vertexCount;
        }

        VertexAttributeBinding(VertexAttribute<TARGET, STORE> target, int offset, int vertexCount, boolean cpuReadable) {
            this.attributes = target;
            this.offset = offset;
            this.vertexCount = vertexCount;
            if (cpuReadable) {
                backing = target.configuration.build(vertexCount);
            }
        }

        public void put(TARGET value) {
            attributes.configuration.map(index, resource.inStride, offset, getBacking(), resource.buffer, value);
            index++;
        }

        public void  rewind() {
            index = 0;
        }

        public void map(int start, int end, STORE input) {
            int loc = 0;
            for (int x = start; x < end; x++) {
                attributes.configuration.map(loc, resource.inStride, offset, getBacking(), resource.buffer, x, input);
                loc++;
            }
        }

        public void put(int vertexIndex, TARGET value) {
            attributes.configuration.map(vertexIndex, resource.inStride, offset, getBacking(), resource.buffer, value);
        }

        /**
         * map store to buffered data
         */
        public void refresh() {
            getBacking().ifPresentOrElse(store -> {
                for (int x = 0; x < vertexCount; x++) {
                    attributes.configuration.map(x, resource.inStride, offset, Optional.empty(), resource.buffer, x, store);
                }
            }, () -> {
                throw new RuntimeException("content is not readable");
            });
        }
    }
}

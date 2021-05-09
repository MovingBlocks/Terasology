// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.assets.mesh.resource;

/**
 * a binding that maps depending on the type of attribute and a resource where the data is committed to
 * @param <TARGET>
 */
public class VertexAttributeBinding<TARGET> {
    private final VertexResource resource;
    private final VertexAttribute<TARGET> attribute;
    private final int offset;
    private int vertexIndex;

    private int version = -1;
    private int numberElements = 0;

    public VertexAttributeBinding(VertexResource resource, int offset, VertexAttribute<TARGET> attribute) {
        this.resource = resource;
        this.attribute = attribute;
        this.offset = offset;
    }

    public VertexResource getResource() {
        return resource;
    }

    public void reserve(int vertCount) {
        resource.ensureCapacity(attribute.configuration.size(vertCount, this.offset, resource));
    }

    public void allocate(int elements) {
        this.resource.reserveElements(elements);
    }

    public void rewind() {
        this.vertexIndex = 0;
    }

    public void setPosition(int index) {
        this.vertexIndex = index;
    }

    public int numberOfElements() {
        if (version != resource.getVersion()) {
            update();
        }
        return numberElements;
    }

    private void  update() {
        if (resource.getInSize() == 0 || resource.getInStride() == 0) {
            numberElements = 0;
        } else {
            numberElements = attribute.configuration.numElements(offset, resource);
        }
        this.version = resource.getVersion();
    }

    /**
     * write a value by the index.
     *
     * @param value the value to commit
     */
    public void put(TARGET value) {
        resource.ensureCapacity(attribute.configuration.size(this.vertexIndex, this.offset, resource));
        attribute.configuration.write(value, this.vertexIndex, this.offset, resource);
        this.vertexIndex++;
        this.resource.mark();
    }

    public void set(int index, TARGET value) {
        attribute.configuration.write(value, index, this.offset, resource);
        this.resource.mark();
    }

    public TARGET get(int index, TARGET dest) {
        return attribute.configuration.read(index, this.offset, resource, dest);
    }
}

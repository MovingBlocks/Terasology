// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.assets.mesh.resource;

/**
 * a binding that maps from a type to a resource where the data is committed to.
 * @param <T>
 */
public class VertexAttributeBinding<T, TImpl extends T> extends VertexBinding {
    private final VertexAttribute<T, TImpl> attribute;

    public VertexAttributeBinding(VertexResource resource, int offset, VertexAttribute<T, TImpl> attribute) {
        super(resource, offset);
        this.attribute = attribute;
    }


    public int elements() {
        return getResource().elements();
    }

    @Override
    public void reserve(int vertCount) {
        resource.reserveElements(vertCount);
    }

    @Override
    public void allocate(int elements) {
        resource.allocateElements(elements);
        resource.mark();
    }

    /**
     * write a value by the index.
     *
     * @param value the value to commit
     */
    public void put(T value) {
        resource.ensureElements(this.vertexIndex + 1);
        attribute.configuration.write(value, this.vertexIndex, this.offset, resource);
        this.vertexIndex++;
        this.resource.mark();
    }

    public void set(int index, T value) {
        attribute.configuration.write(value, index, this.offset, resource);
        this.resource.mark();
    }

    public TImpl get(int index, TImpl dest) {
        return attribute.configuration.read(index, this.offset, resource, dest);
    }
}

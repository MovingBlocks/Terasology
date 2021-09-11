// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.assets.mesh.resource;

public class VertexIntegerAttributeBinding extends VertexBinding {
    private final VertexIntegerAttribute attribute;

    public VertexIntegerAttributeBinding(VertexResource resource, int offset, VertexIntegerAttribute attribute) {
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

    public void put(int value) {
        resource.ensureElements(this.vertexIndex + 1);
        attribute.configuration.write(value, this.vertexIndex, this.offset, resource);
        this.vertexIndex++;
        this.resource.mark();
    }


    /**
     * write all value by the index.
     *
     * @param values the values to commit
     */
    public void putAll(int... values) {
        resource.ensureElements(this.vertexIndex + values.length);
        for (int value : values) {
            attribute.configuration.write(value, this.vertexIndex, this.offset, resource);
            this.vertexIndex++;
            this.resource.mark();
        }
    }


    public void set(int index, int value) {
        attribute.configuration.write(value, index, this.offset, resource);
        this.resource.mark();
    }

    public int get(int index) {
        return attribute.configuration.read(index, this.offset, resource);
    }

}

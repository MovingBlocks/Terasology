// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.assets.mesh.resource;

public class VertexFloatAttributeBinding extends VertexBinding {
    private final VertexFloatAttribute attribute;

    public VertexFloatAttributeBinding(VertexResource resource, int offset, VertexFloatAttribute attribute) {
        super(resource, offset);
        this.attribute = attribute;
    }

    @Override
    public void allocate(int elements) {
        resource.allocateElements(elements);
        resource.mark();
    }

    @Override
    public void reserve(int vertCount) {
        resource.reserveElements(vertCount);
    }

    public void put(float value) {
        resource.ensureElements(this.vertexIndex + 1);
        attribute.configuration.write(value, this.vertexIndex, this.offset, resource);
        this.vertexIndex++;
        this.resource.mark();
    }

    public void set(int index, float value) {
        attribute.configuration.write(value, index, this.offset, resource);
        this.resource.mark();
    }

    public float get(int index) {
        return attribute.configuration.read(index, this.offset, resource);
    }
}

// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.assets.mesh.resource;

public class VertexShortAttributeBinding extends VertexBinding {

    private final VertexShortAttribute attribute;

    public VertexShortAttributeBinding(VertexResource resource, int offset, VertexShortAttribute attribute) {
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

    public void put(short value) {
        resource.ensureElements(this.vertexIndex + 1);
        attribute.configuration.write(value, this.vertexIndex, this.offset, resource);
        this.vertexIndex++;
        this.resource.mark();
    }

    public void set(int index, short value) {
        attribute.configuration.write(value, index, this.offset, resource);
        this.resource.mark();
    }
}

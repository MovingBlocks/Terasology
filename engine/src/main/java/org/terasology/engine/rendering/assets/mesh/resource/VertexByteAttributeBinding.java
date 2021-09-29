// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.assets.mesh.resource;

public class VertexByteAttributeBinding extends VertexBinding {

    private final VertexByteAttribute attribute;

    public VertexByteAttributeBinding(VertexResource resource, int offset, VertexByteAttribute attribute) {
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

    public void put(byte value) {
        resource.ensureElements(this.vertexIndex + 1);
        attribute.configuration.write(value, this.vertexIndex, this.offset, resource);
        this.vertexIndex++;
        this.resource.mark();
    }

    public void set(int index, byte value) {
        attribute.configuration.write(value, index, this.offset, resource);
        this.resource.mark();
    }
}

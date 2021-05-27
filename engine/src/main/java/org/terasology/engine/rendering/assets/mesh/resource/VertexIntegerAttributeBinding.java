// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.assets.mesh.resource;

public class VertexIntegerAttributeBinding extends VertexBinding {
    private final VertexIntegerAttribute attribute;

    public VertexIntegerAttributeBinding(VertexResource resource, int offset, VertexIntegerAttribute attribute) {
        super(resource, offset);
        this.attribute = attribute;
    }
    @Override
    public void allocate(int elements) {
        this.resource.reserveElements(elements);
    }

    @Override
    public void reserve(int vertCount) {
        resource.ensureCapacity(attribute.configuration.size(vertCount, this.offset, resource));
    }

    @Override
    public void allocateElements(int verts) {
        this.inVerts = verts;
        resource.ensureCapacity(attribute.configuration.size(this.inVerts, this.offset, resource));
        resource.mark();
    }

    public void put(int value) {
        if (vertexIndex >= inVerts) {
            inVerts = vertexIndex + 1;
            resource.ensureCapacity(attribute.configuration.size(this.inVerts, this.offset, resource));
        }

        attribute.configuration.write(value, this.vertexIndex, this.offset, resource);
        this.vertexIndex++;
        this.resource.mark();
    }

    public void set(int index, int value) {
        attribute.configuration.write(value, index, this.offset, resource);
        this.resource.mark();
    }

    public int get(int index) {
        return attribute.configuration.read(index, this.offset, resource);
    }

}

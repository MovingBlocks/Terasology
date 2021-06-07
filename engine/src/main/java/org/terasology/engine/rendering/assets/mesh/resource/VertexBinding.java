// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.assets.mesh.resource;

public  abstract class VertexBinding {
    protected final int offset;
    protected final VertexResource resource;
    protected int vertexIndex = 0;

    public VertexBinding(VertexResource resource, int offset) {
        this.offset = offset;
        this.resource = resource;
    }

    public void rewind() {
        this.vertexIndex = 0;
    }

    public void setPosition(int index) {
        this.vertexIndex = index;
    }

    public VertexResource getResource() {
        return resource;
    }

    public int getPosition() {
        return this.vertexIndex;
    }

    public abstract void allocate(int elements);

    public abstract void reserve(int vertCount);


}

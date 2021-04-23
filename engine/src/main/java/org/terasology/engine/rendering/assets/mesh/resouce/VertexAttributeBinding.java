// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.assets.mesh.resouce;

public abstract class VertexAttributeBinding<TARGET> {
    public VertexAttributeBinding(VertexResource resource) {
        this.resource = resource;
    }
    protected VertexResource resource;
    public VertexResource getResource() {
        return resource;
    }
    public abstract TARGET[] getStore();
    public abstract void rewind();
    public abstract int count();
    public abstract void map(int startIndex, int endIndex, float[] arr, int offsetIndex);
    public abstract void put(int vertexIndex, TARGET value);
    public abstract void put(TARGET value);
    public abstract void refresh();
}

// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.assets.mesh;

import org.joml.Vector3f;
import org.terasology.gestalt.assets.Asset;
import org.terasology.gestalt.assets.AssetType;
import org.terasology.gestalt.assets.DisposableResource;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.joml.geom.AABBf;
import org.terasology.joml.geom.AABBfc;

public abstract class Mesh extends Asset<MeshData> {

    protected Mesh(ResourceUrn urn, AssetType<?, MeshData> assetType) {
        super(urn, assetType);
    }

    protected Mesh(ResourceUrn urn, AssetType<?, MeshData> assetType, DisposableResource resource) {
        super(urn, assetType, resource);
    }

    public abstract AABBfc getAABB();

    protected AABBf getBound(MeshData data, AABBf dest) {
        Vector3f[] vertices = this.getVertices();
        if (vertices.length == 0) {
            dest.set(Float.POSITIVE_INFINITY,
                Float.POSITIVE_INFINITY,
                Float.POSITIVE_INFINITY,
                Float.NEGATIVE_INFINITY,
                Float.NEGATIVE_INFINITY,
                Float.NEGATIVE_INFINITY);
            return dest;
        }
        for (Vector3f vertex : vertices) {
            dest.union(vertex);
        }
        return dest;
    }

    public abstract Vector3f[] getVertices();
    public abstract int getVertexCount();

    // TODO: Remove? At least review.
    public abstract void render();
}

// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.assets.mesh;

import gnu.trove.list.TFloatList;
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
        TFloatList vertices = data.getVertices();
        int vertexCount = vertices.size() / 3;
        if (vertexCount == 0) {
            dest.set(Float.POSITIVE_INFINITY,
                Float.POSITIVE_INFINITY,
                Float.POSITIVE_INFINITY,
                Float.NEGATIVE_INFINITY,
                Float.NEGATIVE_INFINITY,
                Float.NEGATIVE_INFINITY);
            return dest;
        }

        dest.minX = vertices.get(0);
        dest.minY = vertices.get(1);
        dest.minZ = vertices.get(2);
        dest.maxX = vertices.get(0);
        dest.maxY = vertices.get(1);
        dest.maxZ = vertices.get(2);

        for (int index = 1; index < vertexCount; ++index) {
            dest.union(vertices.get(3 * index), vertices.get(3 * index + 1), vertices.get(3 * index + 2));

        }
        return dest;
    }

    public abstract TFloatList getVertices();

    // TODO: Remove? At least review.
    public abstract void render();
}

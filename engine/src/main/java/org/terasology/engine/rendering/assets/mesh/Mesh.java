// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.assets.mesh;

import gnu.trove.list.TFloatList;
import org.terasology.assets.Asset;
import org.terasology.assets.AssetType;
import org.terasology.assets.ResourceUrn;
import org.terasology.joml.geom.AABBf;
import org.terasology.joml.geom.AABBfc;

public abstract class Mesh extends Asset<MeshData> {

    protected Mesh(ResourceUrn urn, AssetType<?, MeshData> assetType) {
        super(urn, assetType);
    }

    public abstract AABBfc getAABB();

    protected AABBf getBound(MeshData data, AABBf dest) {
        Float[] vertices = this.getVertices();
        int vertexCount = vertices.length / 3;
        if (vertexCount == 0) {
            dest.set(Float.POSITIVE_INFINITY,
                Float.POSITIVE_INFINITY,
                Float.POSITIVE_INFINITY,
                Float.NEGATIVE_INFINITY,
                Float.NEGATIVE_INFINITY,
                Float.NEGATIVE_INFINITY);
            return dest;
        }

        dest.minX = vertices[0];
        dest.minY = vertices[1];
        dest.minZ = vertices[2];
        dest.maxX = vertices[0];
        dest.maxY = vertices[1];
        dest.maxZ = vertices[2];

        for (int index = 1; index < vertexCount; ++index) {
            dest.union(vertices[3 * index], vertices[3 * index + 1], vertices[3 * index + 2]);

        }
        return dest;
    }

    public abstract Float[] getVertices();
    public abstract int getVertexCount();

    // TODO: Remove? At least review.
    public abstract void render();
}

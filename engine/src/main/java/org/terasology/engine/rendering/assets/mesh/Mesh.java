// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.assets.mesh;

import org.joml.Vector3f;
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
        Vector3f[] vertices = this.getVertices();
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

        dest.minX = vertices[0].x;
        dest.minY = vertices[0].y;
        dest.minZ = vertices[0].z;
        dest.maxX = vertices[0].x;
        dest.maxY = vertices[0].y;
        dest.maxZ = vertices[0].z;

        for (int index = 1; index < vertexCount; ++index) {
            dest.union(vertices[index]);
//            , vertices[3 * index + 1], vertices[3 * index + 2]);
        }
        return dest;
    }

    public abstract Vector3f[] getVertices();
    public abstract int getVertexCount();

    // TODO: Remove? At least review.
    public abstract void render();
}

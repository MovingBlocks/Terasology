// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.assets.mesh;

import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.terasology.engine.rendering.assets.mesh.resource.VertexAttributeBinding;
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
        super(urn, assetType);
        setDisposableResource(resource);
    }

    public abstract AABBfc getAABB();

    protected AABBf getBound(AABBf dest) {
        VertexAttributeBinding<Vector3fc, Vector3f> vertices = this.vertices();
        if (elementCount() == 0) {
            dest.set(Float.POSITIVE_INFINITY,
                    Float.POSITIVE_INFINITY,
                    Float.POSITIVE_INFINITY,
                    Float.NEGATIVE_INFINITY,
                    Float.NEGATIVE_INFINITY,
                    Float.NEGATIVE_INFINITY);
            return dest;
        }
        Vector3f pos = new Vector3f();
        for (int x = 0; x < elementCount(); x++) {
            dest.union(vertices.get(x, pos));
        }
        return dest;
    }

    public abstract VertexAttributeBinding<Vector3fc, Vector3f> vertices();
    public abstract int elementCount();

    // TODO: Remove? At least review.
    public abstract void render();
}

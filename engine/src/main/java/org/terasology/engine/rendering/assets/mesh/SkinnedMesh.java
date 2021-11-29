// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.assets.mesh;

import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.terasology.engine.rendering.assets.mesh.resource.VertexAttributeBinding;
import org.terasology.engine.rendering.assets.mesh.resource.VertexByteAttributeBinding;
import org.terasology.engine.rendering.assets.skeletalmesh.Bone;
import org.terasology.gestalt.assets.Asset;
import org.terasology.gestalt.assets.AssetType;
import org.terasology.gestalt.assets.DisposableResource;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.joml.geom.AABBf;
import org.terasology.joml.geom.AABBfc;

import java.util.List;

public abstract class SkinnedMesh extends Asset<SkinnedMeshData> {

    protected SkinnedMesh(ResourceUrn urn, AssetType<?, SkinnedMeshData> assetType) {
        super(urn, assetType);
    }

    protected SkinnedMesh(ResourceUrn urn, AssetType<?, SkinnedMeshData> assetType, DisposableResource resource) {
        super(urn, assetType, resource);
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

    public abstract VertexByteAttributeBinding boneIndex0();
    public abstract VertexByteAttributeBinding boneIndex1();
    public abstract VertexByteAttributeBinding boneIndex2();
    public abstract VertexByteAttributeBinding boneIndex3();
    public abstract List<Bone> bones();
    public abstract void render();
}

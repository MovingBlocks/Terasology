// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core.subsystem.headless.assets;

import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.terasology.engine.rendering.assets.mesh.Mesh;
import org.terasology.engine.rendering.assets.mesh.MeshData;
import org.terasology.engine.rendering.assets.mesh.resource.VertexAttributeBinding;
import org.terasology.gestalt.assets.AssetType;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.joml.geom.AABBf;
import org.terasology.joml.geom.AABBfc;

public class HeadlessMesh extends Mesh {

    protected MeshData data;
    protected AABBf aabb = new AABBf();

    public HeadlessMesh(ResourceUrn urn, AssetType<?, MeshData> assetType, MeshData meshData) {
        super(urn, assetType);
        reload(meshData);
    }

    @Override
    protected void doReload(MeshData meshData) {
        this.data = meshData;
        getBound(aabb);
    }

    @Override
    public AABBfc getAABB() {
        return aabb;
    }

    @Override
    public VertexAttributeBinding<Vector3fc, Vector3f> vertices() {
        return data.positions();
    }

    @Override
    public int elementCount() {
        return data.positions().getResource().elements();
    }

    @Override
    public void render() {
        // do nothing
    }
}

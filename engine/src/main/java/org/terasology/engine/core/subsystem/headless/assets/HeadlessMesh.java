// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core.subsystem.headless.assets;

import gnu.trove.list.TFloatList;
import org.terasology.engine.math.AABB;
import org.terasology.engine.rendering.assets.mesh.Mesh;
import org.terasology.engine.rendering.assets.mesh.MeshData;
import org.terasology.gestalt.assets.AssetType;
import org.terasology.gestalt.assets.ResourceUrn;

public class HeadlessMesh extends Mesh {

    protected MeshData data;
    protected AABB aabb;

    public HeadlessMesh(ResourceUrn urn, AssetType<?, MeshData> assetType, MeshData meshData) {
        super(urn, assetType);
        reload(meshData);
    }

    @Override
    protected void doReload(MeshData meshData) {
        this.data = meshData;
        this.aabb = AABB.createEncompasing(meshData.getVertices());
    }

    @Override
    public AABB getAABB() {
        return aabb;
    }

    @Override
    public TFloatList getVertices() {
        return data.getVertices();
    }

    @Override
    public void render() {
        // do nothing
    }
}

// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.assets.mesh;

import gnu.trove.list.TFloatList;
import org.terasology.gestalt.assets.Asset;
import org.terasology.gestalt.assets.AssetType;
import org.terasology.gestalt.assets.DisposableResource;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.engine.math.AABB;

public abstract class Mesh extends Asset<MeshData> {

    public static final int VERTEX_SIZE = 3;
    public static final int TEX_COORD_0_SIZE = 2;
    public static final int TEX_COORD_1_SIZE = 3;
    public static final int COLOR_SIZE = 4;
    public static final int NORMAL_SIZE = 3;

    protected Mesh(ResourceUrn urn, AssetType<?, MeshData> assetType) {
        super(urn, assetType);
    }

    protected Mesh(ResourceUrn urn, AssetType<?, MeshData> assetType, DisposableResource disposableResource) {
        super(urn, assetType, disposableResource);
    }

    public abstract AABB getAABB();

    public abstract TFloatList getVertices();

    // TODO: Remove? At least review.
    public abstract void render();
}

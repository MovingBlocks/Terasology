// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core.subsystem.headless.assets;

import org.terasology.engine.rendering.assets.skeletalmesh.Bone;
import org.terasology.engine.rendering.assets.skeletalmesh.SkeletalMesh;
import org.terasology.engine.rendering.assets.skeletalmesh.SkeletalMeshData;
import org.terasology.gestalt.assets.Asset;
import org.terasology.gestalt.assets.AssetType;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.joml.geom.AABBf;

import java.util.Collection;
import java.util.Optional;

public class HeadlessSkeletalMesh extends SkeletalMesh {

    private SkeletalMeshData data;

    public HeadlessSkeletalMesh(ResourceUrn urn, AssetType<?, SkeletalMeshData> assetType, SkeletalMeshData data) {
        super(urn, assetType);
        reload(data);
    }

    @Override
    protected void doReload(SkeletalMeshData skeletalMeshData) {
        this.data = skeletalMeshData;
    }

    @Override
    protected Optional<? extends Asset<SkeletalMeshData>> doCreateCopy(ResourceUrn instanceUrn, AssetType<?, SkeletalMeshData> parentAssetType) {
        return Optional.of(new HeadlessSkeletalMesh(instanceUrn, parentAssetType, data));
    }

    @Override
    public int getVertexCount() {
        return data.getVertexCount();
    }

    @Override
    public Collection<Bone> getBones() {
        return data.getBones();
    }

    @Override
    public Bone getBone(String boneName) {
        return data.getBone(boneName);
    }

    @Override
    public AABBf getStaticAabb() {
        return data.getStaticAABB();
    }
}

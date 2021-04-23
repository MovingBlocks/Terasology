// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.assets.skeletalmesh;

import org.terasology.assets.Asset;
import org.terasology.assets.AssetType;
import org.terasology.assets.ResourceUrn;
import org.terasology.joml.geom.AABBf;

import java.util.Collection;

/**
 */
public abstract class SkeletalMesh extends Asset<SkeletalMeshData> {

    protected SkeletalMesh(ResourceUrn urn, AssetType<?, SkeletalMeshData> assetType) {
        super(urn, assetType);
    }

    public abstract Collection<Bone> getBones();

    public abstract Bone getBone(String boneName);

    /**
     *
     * @return the boundings of the mesh when it its not being animated.
     */
    public abstract AABBf getStaticAabb();
}

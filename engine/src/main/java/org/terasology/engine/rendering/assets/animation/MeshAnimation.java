// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.assets.animation;

import org.terasology.assets.Asset;
import org.terasology.assets.AssetType;
import org.terasology.assets.ResourceUrn;
import org.terasology.engine.rendering.assets.skeletalmesh.SkeletalMesh;
import org.terasology.joml.geom.AABBf;

/**
 */
public abstract class MeshAnimation extends Asset<MeshAnimationData> {

    protected MeshAnimation(ResourceUrn urn, AssetType<?, MeshAnimationData> assetType) {
        super(urn, assetType);
    }

    public abstract boolean isValidAnimationFor(SkeletalMesh mesh);

    public abstract int getBoneCount();

    public abstract int getFrameCount();

    public abstract MeshAnimationFrame getFrame(int frame);

    public abstract String getBoneName(int index);

    public abstract float getTimePerFrame();

    public abstract AABBf getAabb();
}

// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.assets.animation;

import org.terasology.engine.rendering.assets.mesh.SkinnedMesh;
import org.terasology.gestalt.assets.Asset;
import org.terasology.gestalt.assets.AssetType;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.joml.geom.AABBf;

public abstract class MeshAnimation extends Asset<MeshAnimationData> {

    protected MeshAnimation(ResourceUrn urn, AssetType<?, MeshAnimationData> assetType) {
        super(urn, assetType);
    }

    public abstract boolean isValidAnimationFor(SkinnedMesh mesh);

    public abstract int getBoneCount();

    public abstract int getFrameCount();

    public abstract MeshAnimationFrame getFrame(int frame);

    public abstract String getBoneName(int index);

    public abstract float getTimePerFrame();

    public abstract AABBf getAabb();

    public float getDuration() {
        return  getTimePerFrame() * (getFrameCount() - 1);
    }
}

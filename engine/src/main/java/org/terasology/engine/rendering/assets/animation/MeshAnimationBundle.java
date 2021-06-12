// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.assets.animation;

import org.terasology.gestalt.assets.Asset;
import org.terasology.gestalt.assets.AssetType;
import org.terasology.gestalt.assets.ResourceUrn;

import java.util.Optional;

/**
 * An asset providing a set of animation assets
 */
public class MeshAnimationBundle extends Asset<MeshAnimationBundleData> {

    private MeshAnimationBundleData data;

    public MeshAnimationBundle(ResourceUrn urn, AssetType<?, MeshAnimationBundleData> assetType, MeshAnimationBundleData data) {
        super(urn, assetType);
        reload(data);
    }

    public Optional<MeshAnimationData> getAnimation(ResourceUrn urn) {
        return Optional.ofNullable(data.getMeshAnimation(urn));
    }

    @Override
    protected void doReload(MeshAnimationBundleData bundleData) {
        this.data = bundleData;
    }


}

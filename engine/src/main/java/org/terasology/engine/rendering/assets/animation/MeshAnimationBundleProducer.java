// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.assets.animation;

import org.terasology.assets.AbstractFragmentDataProducer;
import org.terasology.assets.ResourceUrn;
import org.terasology.assets.management.AssetManager;
import org.terasology.assets.module.annotations.RegisterAssetDataProducer;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

/**
 * Generic producer for obtaining animation assets from animation asset bundles
 */
@RegisterAssetDataProducer
public class MeshAnimationBundleProducer extends AbstractFragmentDataProducer<MeshAnimationData, MeshAnimationBundle, MeshAnimationBundleData> {

    public MeshAnimationBundleProducer(AssetManager assetManager) {
        super(assetManager, MeshAnimationBundle.class, true);
    }

    @Override
    protected Optional<MeshAnimationData> getFragmentData(ResourceUrn urn, MeshAnimationBundle rootAsset) {
        return rootAsset.getAnimation(urn);
    }

    @Override
    public Set<ResourceUrn> getAvailableAssetUrns() {
        return Collections.emptySet();
    }
}

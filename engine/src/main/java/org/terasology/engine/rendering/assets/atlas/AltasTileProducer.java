// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.assets.atlas;

import org.terasology.gestalt.assets.AbstractFragmentDataProducer;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.engine.rendering.assets.texture.subtexture.SubtextureData;
import org.terasology.gestalt.assets.management.AssetManager;
import org.terasology.gestalt.assets.module.annotations.RegisterAssetDataProducer;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

@RegisterAssetDataProducer
public class AltasTileProducer extends AbstractFragmentDataProducer<SubtextureData, Atlas, AtlasData> {

    public AltasTileProducer(AssetManager assetManager) {
        super(assetManager, Atlas.class, true);
    }

    @Override
    protected Optional<SubtextureData> getFragmentData(ResourceUrn urn, Atlas rootAsset) {
        return rootAsset.getSubtexture(urn);
    }

    @Override
    public Set<ResourceUrn> getAvailableAssetUrns() {
        return Collections.emptySet();
    }
}

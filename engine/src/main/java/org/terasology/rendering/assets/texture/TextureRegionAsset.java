// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.rendering.assets.texture;

import org.terasology.assets.Asset;
import org.terasology.assets.AssetData;
import org.terasology.assets.AssetType;
import org.terasology.assets.ResourceUrn;

/**
 */
public abstract class TextureRegionAsset<T extends AssetData> extends Asset<T> implements TextureRegion {

    protected TextureRegionAsset(ResourceUrn urn, AssetType<?, T> assetType) {
        super(urn, assetType);
    }
}

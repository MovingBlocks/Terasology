// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.assets.texture;

import org.terasology.gestalt.assets.Asset;
import org.terasology.gestalt.assets.AssetData;
import org.terasology.gestalt.assets.AssetType;
import org.terasology.gestalt.assets.DisposableResource;
import org.terasology.gestalt.assets.ResourceUrn;

public abstract class TextureRegionAsset<T extends AssetData> extends Asset<T> implements TextureRegion {

    protected TextureRegionAsset(ResourceUrn urn, AssetType<?, T> assetType) {
        super(urn, assetType);
    }

    protected TextureRegionAsset(ResourceUrn urn, AssetType<?, T> assetType, DisposableResource disposableResource) {
        super(urn, assetType);
        setDisposableResource(disposableResource);
    }
}

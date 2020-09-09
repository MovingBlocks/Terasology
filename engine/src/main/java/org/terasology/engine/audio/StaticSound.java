// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.audio;

import org.terasology.gestalt.assets.AssetType;
import org.terasology.gestalt.assets.DisposableResource;
import org.terasology.gestalt.assets.ResourceUrn;

/**
 * Interface for a non-streamed sound - these sounds are loaded entirely into memory.
 */
public abstract class StaticSound extends Sound<StaticSoundData> {

    /**
     * The constructor for an asset. It is suggested that implementing classes provide a constructor taking both the
     * urn, and an initial AssetData to load.
     *
     * @param urn The urn identifying the asset.
     * @param assetType The asset type this asset belongs to.
     * @param disposableResource The disposable handler for asset
     */
    protected StaticSound(ResourceUrn urn, AssetType<?, StaticSoundData> assetType,
                          DisposableResource disposableResource) {
        super(urn, assetType, disposableResource);
    }

    /**
     * The constructor for an asset. It is suggested that implementing classes provide a constructor taking both the
     * urn, and an initial AssetData to load.
     *
     * @param urn The urn identifying the asset.
     * @param assetType The asset type this asset belongs to.
     */
    protected StaticSound(ResourceUrn urn, AssetType<?, StaticSoundData> assetType) {
        super(urn, assetType);
    }

    /**
     * Returns sound sample length in seconds
     *
     * @return
     */
    public abstract float getLength();
}

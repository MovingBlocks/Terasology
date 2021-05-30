// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.audio;

import org.terasology.gestalt.assets.AssetType;
import org.terasology.gestalt.assets.DisposableResource;
import org.terasology.gestalt.assets.ResourceUrn;

/**
 * Interface for a sound that is streamed from storage. The entire sound is not loaded into memory
 */
public abstract class StreamingSound extends Sound<StreamingSoundData> {

    /**
     * The constructor for an asset. It is suggested that implementing classes provide a constructor taking both the urn, and an initial AssetData to load.
     *
     * @param urn       The urn identifying the asset.
     * @param assetType The asset type this asset belongs to.
     */
    protected StreamingSound(ResourceUrn urn, AssetType<?, StreamingSoundData> assetType, DisposableResource resource) {
        super(urn, assetType, resource);
    }

    /**
     * The constructor for an asset. It is suggested that implementing classes provide a constructor taking both the urn, and an initial AssetData to load.
     *
     * @param urn       The urn identifying the asset.
     * @param assetType The asset type this asset belongs to.
     */
    protected StreamingSound(ResourceUrn urn, AssetType<?, StreamingSoundData> assetType) {
        super(urn, assetType);
    }

    /**
     * Reset sound state (clears buffers, reset cached info)
     */
    public abstract void reset();

}

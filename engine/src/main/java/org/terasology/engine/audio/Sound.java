// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.audio;

import org.terasology.gestalt.assets.Asset;
import org.terasology.gestalt.assets.AssetData;
import org.terasology.gestalt.assets.AssetType;
import org.terasology.gestalt.assets.DisposableResource;
import org.terasology.gestalt.assets.ResourceUrn;

/**
 * An abstract class
 * @param <T> The asset type this asset belongs to
 */
public abstract class Sound<T extends AssetData> extends Asset<T> implements org.terasology.nui.asset.Sound {

    /**
     * The constructor for an asset.
     * It is suggested that implementing classes provide a constructor taking both the urn, and an initial AssetData to load.
     *
     * @param urn       The urn identifying the asset.
     * @param assetType The asset type this asset belongs to.
     */
    protected Sound(ResourceUrn urn, AssetType<?, T> assetType, DisposableResource resource) {
        super(urn, assetType);
        setDisposableResource(resource);
    }

    /**
     * The constructor for an asset.
     * It is suggested that implementing classes provide a constructor taking both the urn, and an initial AssetData to load.
     *
     * @param urn       The urn identifying the asset.
     * @param assetType The asset type this asset belongs to.
     */
    protected Sound(ResourceUrn urn, AssetType<?, T> assetType) {
        super(urn, assetType);
    }

    /**
     * @return channels amount of sound (1 - mono, 2 - stereo)
     */
    public abstract int getChannels();

    /**
     * @return sampling rate of sound (example 44100)
     */
    public abstract int getSamplingRate();

    /**
     * @return the size of the sound buffer
     */
    public abstract int getBufferSize();

    /**
     * Plays the sound at full volume.
     */
    public abstract void play();

    /**
     * Plays the sound at the given volume.
     * @param volume
     */
    public abstract void play(float volume);

}

/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.audio;

import org.terasology.assets.Asset;
import org.terasology.assets.AssetData;
import org.terasology.assets.AssetType;
import org.terasology.assets.ResourceUrn;

public abstract class Sound<T extends AssetData> extends Asset<T> {

    /**
     * The constructor for an asset. It is suggested that implementing classes provide a constructor taking both the urn, and an initial AssetData to load.
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

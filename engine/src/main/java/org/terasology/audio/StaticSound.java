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

import org.terasology.assets.AssetType;
import org.terasology.assets.ResourceUrn;

/**
 * Interface for a non-streamed sound - these sounds are loaded entirely into memory.
 */
public abstract class StaticSound extends Sound<StaticSoundData> {

    /**
     * The constructor for an asset. It is suggested that implementing classes provide a constructor taking both the urn, and an initial AssetData to load.
     *
     * @param urn       The urn identifying the asset.
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

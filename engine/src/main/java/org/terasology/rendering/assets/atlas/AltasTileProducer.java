/*
 * Copyright 2015 MovingBlocks
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
package org.terasology.rendering.assets.atlas;

import org.terasology.gestalt.assets.AbstractFragmentDataProducer;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.assets.management.AssetManager;
import org.terasology.gestalt.assets.module.annotations.RegisterAssetDataProducer;
import org.terasology.rendering.assets.texture.subtexture.SubtextureData;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

/**
 */
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

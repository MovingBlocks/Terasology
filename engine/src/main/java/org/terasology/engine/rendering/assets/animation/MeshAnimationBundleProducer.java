/*
 * Copyright 2019 MovingBlocks
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
package org.terasology.rendering.assets.animation;

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

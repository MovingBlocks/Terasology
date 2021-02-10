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

import org.terasology.assets.Asset;
import org.terasology.assets.AssetType;
import org.terasology.assets.ResourceUrn;

import java.util.Optional;

/**
 * An asset providing a set of animation assets
 */
public class MeshAnimationBundle extends Asset<MeshAnimationBundleData> {

    private MeshAnimationBundleData data;

    public MeshAnimationBundle(ResourceUrn urn, AssetType<?, MeshAnimationBundleData> assetType, MeshAnimationBundleData data) {
        super(urn, assetType);
        reload(data);
    }

    public Optional<MeshAnimationData> getAnimation(ResourceUrn urn) {
        return Optional.ofNullable(data.getMeshAnimation(urn));
    }

    @Override
    protected void doReload(MeshAnimationBundleData data) {
        this.data = data;
    }


}

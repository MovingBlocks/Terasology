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

import com.google.common.collect.ImmutableMap;
import org.terasology.assets.AssetData;
import org.terasology.assets.ResourceUrn;

import java.util.Map;

/**
 * Data for a collection of animation assets
 */
public class MeshAnimationBundleData implements AssetData {

    private final Map<ResourceUrn, MeshAnimationData> meshAnimations;

    public MeshAnimationBundleData(Map<ResourceUrn, MeshAnimationData> animations) {
        this.meshAnimations = ImmutableMap.copyOf(animations);
    }

    public Map<ResourceUrn, MeshAnimationData> getMeshAnimations() {
        return meshAnimations;
    }

    public MeshAnimationData getMeshAnimation(ResourceUrn urn) {
        return meshAnimations.get(urn);
    }
}

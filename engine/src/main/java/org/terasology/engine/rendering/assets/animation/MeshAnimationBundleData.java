// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.assets.animation;

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

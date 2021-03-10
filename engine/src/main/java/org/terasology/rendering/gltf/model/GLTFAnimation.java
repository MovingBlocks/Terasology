// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.gltf.model;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * A GLTF keyframe animation. See https://github.com/KhronosGroup/glTF/blob/master/specification/2.0/README.md#animation for details
 */
public class GLTFAnimation {

    private String name = "";
    private List<GLTFChannel> channels = Lists.newArrayList();
    private List<GLTFAnimationSampler> samplers = Lists.newArrayList();

    /**
     * @return The name of the animation
     */
    public String getName() {
        return name;
    }

    /**
     * @return The samplers linking to accessors providing the data that drives the animation
     */
    public List<GLTFAnimationSampler> getSamplers() {
        return samplers;
    }

    /**
     * @return The channels that apply samplers to animate nodes
     */
    public List<GLTFChannel> getChannels() {
        return channels;
    }
}

// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.gltf.model;

/**
 * GLTF Channel maps an animation sampler to a node value to animate.
 * See https://github.com/KhronosGroup/glTF/blob/master/specification/2.0/README.md#channel for details
 */
public class GLTFChannel {
    private int sampler;
    private GLTFChannelTarget target;

    /**
     * @return The index of the sampler providing animation data
     */
    public int getSampler() {
        return sampler;
    }

    /**
     * @return Details of the value to animate
     */
    public GLTFChannelTarget getTarget() {
        return target;
    }
}

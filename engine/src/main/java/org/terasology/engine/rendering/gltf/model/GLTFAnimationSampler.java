// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.gltf.model;

/**
 * GLTF Animation Sampler maps accessors with an algorithm to produce animation data. See https://github.com/KhronosGroup/glTF/blob/master/specification/2.0/README.md#animation-sampler for details
 */
public class GLTFAnimationSampler {
    private int input;
    private GLTFInterpolation interpolation;
    private int output;

    /**
     * @return Input data, typically time.
     */
    public int getInput() {
        return input;
    }

    /**
     * @return Output data, such as positions or rotations
     */
    public int getOutput() {
        return output;
    }

    /**
     * @return The interpolation algorithm for how to interpolate between output values for intermediate input values
     */
    public GLTFInterpolation getInterpolation() {
        return interpolation;
    }
}

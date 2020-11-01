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
package org.terasology.rendering.gltf.model;

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

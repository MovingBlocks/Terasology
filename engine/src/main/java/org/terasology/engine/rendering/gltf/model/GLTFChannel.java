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
 * GLTF Channel maps an animation sampler to a node value to animate. See https://github.com/KhronosGroup/glTF/blob/master/specification/2.0/README.md#channel for details
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

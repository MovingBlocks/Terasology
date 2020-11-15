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

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
 * GLTF Channel Target is the details of what to animate with a GLTF Channel. See https://github.com/KhronosGroup/glTF/blob/master/specification/2.0/README.md#target for details
 */
public class GLTFChannelTarget {
    private Integer node;
    private GLTFChannelPath path;

    /**
     * @return The index of the node to animate. Apparently optional, I don't know what it means if it is missing (maybe just for mesh with a single node?)
     */
    public Integer getNode() {
        return node;
    }

    /**
     * @return What is being animated.
     */
    public GLTFChannelPath getPath() {
        return path;
    }
}

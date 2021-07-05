// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.gltf.model;

/**
 * GLTF Channel Target is the details of what to animate with a GLTF Channel.
 * <p>
 * See <a href="https://github.com/KhronosGroup/glTF/blob/master/specification/2.0/README.md#target">
 *     glTF Specification - target
 *     </a> for details.
 */
public class GLTFChannelTarget {
    private Integer node;
    private GLTFChannelPath path;

    /**
     * @return The index of the node to animate.
     * Apparently optional, I don't know what it means if it is missing (maybe just for mesh with a single node?)
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

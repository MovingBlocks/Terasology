// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.gltf.model;

/**
 * Describes the location and length of a byte buffer.
 * See https://github.com/KhronosGroup/glTF/blob/master/specification/2.0/README.md#reference-buffer for details
 */
public class GLTFBuffer {
    private String uri = "";
    private int byteLength;
    private String name = "";

    /**
     * @return The name of the buffer
     */
    public String getName() {
        return name;
    }

    /**
     * @return A uri location the buffer
     */
    public String getUri() {
        return uri;
    }

    /**
     * @return The length of the buffer
     */
    public int getByteLength() {
        return byteLength;
    }

    @Override
    public String toString() {
        return "GLTFBuffer('" + name + "')";
    }
}

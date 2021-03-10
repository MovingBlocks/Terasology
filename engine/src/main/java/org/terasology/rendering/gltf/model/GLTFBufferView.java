// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.gltf.model;

import javax.annotation.Nullable;

/**
 * Describes a view into a byte buffer - where to start, the distance between elements (if interleaved) and a the length of interest.
 * See https://github.com/KhronosGroup/glTF/blob/master/specification/2.0/README.md#reference-bufferview for details
 */
public class GLTFBufferView {
    private int buffer;
    private int byteOffset = 0;
    private int byteLength;
    private int byteStride;
    private GLTFTargetBuffer target;
    private String name = "";

    /**
     * @return The index of the buffer this view sits over
     */
    public int getBuffer() {
        return buffer;
    }

    /**
     * @return The offset into the byte buffer
     */
    public int getByteOffset() {
        return byteOffset;
    }

    /**
     * @return The length of the view over the buffer
     */
    public int getByteLength() {
        return byteLength;
    }

    /**
     * @return The byte distance between elements
     */
    public int getByteStride() {
        return byteStride;
    }

    /**
     * @return The type of OpenGL buffer the data is intended for
     */
    @Nullable
    public GLTFTargetBuffer getTarget() {
        return target;
    }

    /**
     * @return The name of the buffer view
     */
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "GLTFBufferView('" + name + "')";
    }
}

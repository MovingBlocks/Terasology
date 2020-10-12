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

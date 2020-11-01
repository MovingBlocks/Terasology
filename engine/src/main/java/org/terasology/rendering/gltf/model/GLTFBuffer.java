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
 * Describes the location and length of a byte buffer. See https://github.com/KhronosGroup/glTF/blob/master/specification/2.0/README.md#reference-buffer for details
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

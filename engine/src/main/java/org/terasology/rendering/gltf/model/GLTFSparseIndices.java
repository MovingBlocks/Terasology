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
 * Details where to source the indices of elements to override
 */
public class GLTFSparseIndices {
    private int bufferView;
    private int byteOffset;
    private GLTFComponentType componentType;

    /**
     * @return The index of the buffer view providing the indices
     */
    public int getBufferView() {
        return bufferView;
    }

    /**
     * @return The byte offset when reading from the buffer view
     */
    public int getByteOffset() {
        return byteOffset;
    }

    /**
     * @return The type used to represent the indices
     */
    public GLTFComponentType getComponentType() {
        return componentType;
    }
}

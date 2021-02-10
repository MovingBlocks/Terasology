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
 * Details where to source the override values
 */
public class GLTFSparseValues {
    private int bufferView;
    private int byteOffset;

    /**
     * @return The offset when reading from the buffer view
     */
    public int getByteOffset() {
        return byteOffset;
    }

    /**
     * @return The index of the buffer view to source the values from
     */
    public int getBufferView() {
        return bufferView;
    }
}

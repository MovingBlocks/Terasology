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
 * GLTFSparse provides sparse storage of overrides for attributes. See https://github.com/KhronosGroup/glTF/blob/master/specification/2.0/README.md#reference-sparse for details
 */
public class GLTFSparse {

    private int count;
    private GLTFSparseIndices indices;
    private GLTFSparseValues values;

    /**
     * @return The number of entries
     */
    public int getCount() {
        return count;
    }

    /**
     * @return Details the indices of elements to override
     */
    public GLTFSparseIndices getIndices() {
        return indices;
    }

    /**
     * @return Details the values to override elements with
     */
    public GLTFSparseValues getValues() {
        return values;
    }
}

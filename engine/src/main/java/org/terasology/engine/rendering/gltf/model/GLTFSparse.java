// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.gltf.model;

/**
 * GLTFSparse provides sparse storage of overrides for attributes.
 * <p>
 * See <a href="https://github.com/KhronosGroup/glTF/blob/master/specification/2.0/README.md#reference-sparse">
 *     glTF Specification - sparse
 *     </a> for details.
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

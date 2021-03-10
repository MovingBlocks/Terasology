// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.gltf.model;

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

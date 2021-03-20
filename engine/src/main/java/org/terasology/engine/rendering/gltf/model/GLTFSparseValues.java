// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.gltf.model;

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

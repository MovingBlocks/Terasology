// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.gltf.model;

/**
 * The dimensional type of an attribute
 */
public enum GLTFAttributeType {
    SCALAR(1),
    VEC2(2),
    VEC3(3),
    VEC4(4),
    MAT2(4),
    MAT3(9),
    MAT4(16);

    private final int dim;

    GLTFAttributeType(int dimensions) {
        this.dim = dimensions;
    }

    /**
     * @return The number of values forming an element
     */
    public int getDimension() {
        return dim;
    }
}

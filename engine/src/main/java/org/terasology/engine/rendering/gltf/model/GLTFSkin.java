// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.gltf.model;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;

/**
 * GLTFSSkin defines the joints and matrices of a skeleton.
 * See https://github.com/KhronosGroup/glTF/blob/master/specification/2.0/README.md#reference-skin for details
 */
public class GLTFSkin {
    private Integer inverseBindMatrices;
    private Integer skeleton;
    private TIntList joints = new TIntArrayList();
    private String name = "";

    /**
     * @return The index of an accessor containing 4x4 floating point inverseBind matrices.
     * If null, then each matrix can be considered to be an identity matrix.
     */
    public Integer getInverseBindMatrices() {
        return inverseBindMatrices;
    }

    /**
     * @return The index of the root node of the skeleton
     */
    public Integer getSkeleton() {
        return skeleton;
    }

    /**
     * @return The name of the skeleton
     */
    public String getName() {
        return name;
    }

    /**
     * @return The indices of the nodes corresponding to bones int he skeleton
     */
    public TIntList getJoints() {
        return joints;
    }

    @Override
    public String toString() {
        return "GLTFSkin('" + name + "')";
    }
}

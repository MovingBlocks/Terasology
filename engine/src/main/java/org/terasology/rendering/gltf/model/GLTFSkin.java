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

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;

/**
 * GLTFSSkin defines the joints and matrices of a skeleton. See https://github.com/KhronosGroup/glTF/blob/master/specification/2.0/README.md#reference-skin for details
 */
public class GLTFSkin {
    private Integer inverseBindMatrices;
    private Integer skeleton;
    private TIntList joints = new TIntArrayList();
    private String name = "";

    /**
     * @return The index of an accessor containing 4x4 floating point inverseBind matrices. If null, then each matrix can be considered to be an identity matrix.
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

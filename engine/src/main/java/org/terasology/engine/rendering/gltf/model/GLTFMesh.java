// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.gltf.model;

import com.google.common.collect.Lists;
import gnu.trove.list.TFloatList;

import javax.annotation.Nullable;
import java.util.List;

/**
 * A glTF mesh is composed of one or more primitives. See https://github.com/KhronosGroup/glTF/blob/master/specification/2.0/README.md#reference-mesh for details.
 */
public class GLTFMesh {
    private List<GLTFPrimitive> primitives = Lists.newArrayList();
    private TFloatList weights;
    private String name = "";

    /**
     * @return A list of primitives composing the mesh
     */
    public List<GLTFPrimitive> getPrimitives() {
        return primitives;
    }

    /**
     * @return A list of weights to apply to morph targets.
     */
    @Nullable
    public TFloatList getWeights() {
        return weights;
    }

    /**
     * @return The name of the mesh
     */
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "GLTFMesh('" + name + "')";
    }
}

// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.gltf.model;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;

import javax.annotation.Nullable;

/**
 * Describes a scene composed of glTF nodes.
 * See https://github.com/KhronosGroup/glTF/blob/master/specification/2.0/README.md#reference-scene for details.
 */
public class GLTFScene {
    private String name = "";
    private TIntList nodes = new TIntArrayList();

    /**
     * @return The name of the scene
     */
    @Nullable
    public String getName() {
        return name;
    }

    /**
     * @return Indices of each root node composing the scene
     */
    @Nullable
    public TIntList getNodes() {
        return nodes;
    }

    @Override
    public String toString() {
        return "GLTFScene('" + name + "')";
    }
}

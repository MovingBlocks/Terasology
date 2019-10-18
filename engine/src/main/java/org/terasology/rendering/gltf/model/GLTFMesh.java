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

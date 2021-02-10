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

import javax.annotation.Nullable;

/**
 * Describes a scene composed of glTF nodes. See https://github.com/KhronosGroup/glTF/blob/master/specification/2.0/README.md#reference-scene for details.
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

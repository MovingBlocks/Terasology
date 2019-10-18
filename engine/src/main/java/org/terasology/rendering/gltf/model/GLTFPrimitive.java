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

import javax.annotation.Nullable;
import java.util.Map;

/**
 * A glTF primitive describes a single piece of geometry to be rendered (either as a single drawcall or batched).
 * See https://github.com/KhronosGroup/glTF/blob/master/specification/2.0/README.md#reference-primitive for details.
 */
public class GLTFPrimitive {
    private Map<String, Integer> attributes;
    private Integer indices;
    private Integer material;
    private GLTFMode mode = GLTFMode.TRIANGLES;

    /**
     * @return A map of attribute name to Accessor index.
     */
    public Map<String, Integer> getAttributes() {
        return attributes;
    }

    /**
     * If no index for the indices accessor is provided, the primitive should be drawn using drawArrays.
     * @return The index of the accessor providing indices.
     */
    @Nullable
    public Integer getIndices() {
        return indices;
    }

    /**
     * @return The index of the material to render the primitive with
     */
    @Nullable
    public Integer getMaterial() {
        return material;
    }

    /**
     * @return The rendering mode to use to render this primitive
     */
    public GLTFMode getMode() {
        return mode;
    }
}

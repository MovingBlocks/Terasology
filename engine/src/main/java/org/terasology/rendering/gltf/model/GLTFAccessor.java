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

import gnu.trove.list.TFloatList;

import javax.annotation.Nullable;

/**
 * GLTFAccessor provides details on how to interpret a buffer view. See https://github.com/KhronosGroup/glTF/blob/master/specification/2.0/README.md#reference-accessor for details
 */
public class GLTFAccessor {
    private Integer bufferView;
    private int byteOffset = 0;
    private GLTFComponentType componentType;
    private boolean normalised = false;
    private int count;
    private GLTFAttributeType type;
    private TFloatList max;
    private TFloatList min;
    private GLTFSparse sparse;
    private String name = "";

    /**
     * @return Index of the buffer view this accessor works with. If unspecified then the content should be considered to be all zeros.
     */
    @Nullable
    public Integer getBufferView() {
        return bufferView;
    }

    /**
     * @return The byte offset of the first byte this accessor accesses from the bufferView.
     */
    public int getByteOffset() {
        return byteOffset;
    }

    /**
     * @return The data type of elements stored in the bufferView.
     */
    public GLTFComponentType getComponentType() {
        return componentType;
    }

    /**
     * @return The name of the accessor
     */
    public String getName() {
        return name;
    }

    /**
     * @return Whether integer values should be normalized to [0,1] (unsigned) or [-1,1] (signed) when accessed.
     */
    public boolean isNormalised() {
        return normalised;
    }

    /**
     * @return The number of elements to access
     */
    public int getCount() {
        return count;
    }

    /**
     * @return The dimensional type of the elements.
     */
    public GLTFAttributeType getType() {
        return type;
    }

    /**
     * @return The maximum bounds of each part of each element.
     */
    @Nullable
    public TFloatList getMax() {
        return max;
    }

    /**
     * @return The minimum bounds of each part of each element.
     */
    @Nullable
    public TFloatList getMin() {
        return min;
    }

    /**
     * @return If specified, provides overrides for specific values of the buffer view
     */
    @Nullable
    public GLTFSparse getSparse() {
        return sparse;
    }

    @Override
    public String toString() {
        return "GLTFAccessor('" + name + "')";
    }
}

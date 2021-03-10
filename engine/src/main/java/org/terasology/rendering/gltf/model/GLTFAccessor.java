// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.gltf.model;

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

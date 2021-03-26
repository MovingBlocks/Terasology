// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.gltf.model;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * glTFComponentType defines the types of items that can be contained in a buffer.
 */
public enum GLTFComponentType {
    BYTE(5120, 1, false),
    UNSIGNED_BYTE(5121, 1, true),
    SHORT(5122, 2, false),
    UNSIGNED_SHORT(5123, 2, true),
    UNSIGNED_INT(5125, 4, true),
    FLOAT(5126, 4, false);

    private static TIntObjectMap<GLTFComponentType> codeToType;

    private final int code;
    private final int byteLength;
    private final boolean validForIndices;

    static {
        codeToType = new TIntObjectHashMap<>();
        for (GLTFComponentType type : GLTFComponentType.values()) {
            codeToType.put(type.code, type);
        }
    }

    GLTFComponentType(int code, int byteLength, boolean validForIndices) {
        this.code = code;
        this.byteLength = byteLength;
        this.validForIndices = validForIndices;
    }

    /**
     * @return If this is a valid component type for representing indices
     */
    public boolean isValidForIndices() {
        return validForIndices;
    }

    /**
     * @return The code used to specify this type in a glTF asset
     */
    public int getCode() {
        return code;
    }

    /**
     * @return The length in bytes of an item of this type
     */
    public int getByteLength() {
        return byteLength;
    }

    /**
     * @param code
     * @return The component type specified by the provided code
     */
    public static GLTFComponentType getTypeFromCode(int code) {
        return codeToType.get(code);
    }


}

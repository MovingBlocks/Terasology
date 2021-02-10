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

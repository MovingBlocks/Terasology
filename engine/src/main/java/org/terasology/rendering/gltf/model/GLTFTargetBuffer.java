// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.gltf.model;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * Enumeration of the possible OpenGL buffer types that a buffer view can target.
 */
public enum GLTFTargetBuffer {

    ARRAY_BUFFER(34962),
    ELEMENT_ARRAY_BUFFER(34963);

    private static TIntObjectMap<GLTFTargetBuffer> codeToType;

    private int code;

    GLTFTargetBuffer(int code) {
        this.code = code;
    }

    static {
        codeToType = new TIntObjectHashMap<>();
        for (GLTFTargetBuffer type : GLTFTargetBuffer.values()) {
            codeToType.put(type.code, type);
        }
    }

    /**
     * @return The code of the buffer type in glTF
     */
    public int getCode() {
        return code;
    }

    /**
     * @param code
     * @return The GLTFTargetBuffer for the given code.
     */
    public static GLTFTargetBuffer getTypeFromCode(int code) {
        return codeToType.get(code);
    }

}

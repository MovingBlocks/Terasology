// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.gltf.model;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * Rendering mode for drawing a glTF primitive
 */
public enum GLTFMode {
    POINTS(0),
    LINES(1),
    LINE_LOOP(2),
    LINE_STRIP(3),
    TRIANGLES(4),
    TRIANGLE_STRIP(5),
    TRIANGLE_FAN(6);

    private static final TIntObjectMap<GLTFMode> codeToMode;
    private final int code;

    static {
        codeToMode = new TIntObjectHashMap<>();
        for (GLTFMode mode : GLTFMode.values()) {
            codeToMode.put(mode.code, mode);
        }
    }

    GLTFMode(int code) {
        this.code = code;
    }

    /**
     *
     * @param code
     * @return The GLTFMode for the given code
     */
    public static GLTFMode fromCode(int code) {
        return codeToMode.get(code);
    }


}

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

    private static final TIntObjectMap<GLTFMode> CODE_TO_MODE;
    private final int code;

    static {
        CODE_TO_MODE = new TIntObjectHashMap<>();
        for (GLTFMode mode : GLTFMode.values()) {
            CODE_TO_MODE.put(mode.code, mode);
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
        return CODE_TO_MODE.get(code);
    }


}

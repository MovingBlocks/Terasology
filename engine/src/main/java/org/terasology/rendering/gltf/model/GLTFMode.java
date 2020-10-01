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

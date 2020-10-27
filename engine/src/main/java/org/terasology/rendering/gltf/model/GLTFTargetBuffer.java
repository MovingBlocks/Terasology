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

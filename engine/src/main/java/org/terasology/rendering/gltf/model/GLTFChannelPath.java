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

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * GLTFChannelPath is an enumeration of values of a node that can be animated
 */
public enum GLTFChannelPath {
    ROTATION("rotation", GLTFAttributeType.VEC4, GLTFComponentType.FLOAT),
    TRANSLATION("translation", GLTFAttributeType.VEC3, GLTFComponentType.FLOAT),
    SCALE("scale", GLTFAttributeType.VEC3, GLTFComponentType.FLOAT),
    WEIGHTS("weights", GLTFAttributeType.SCALAR, GLTFComponentType.FLOAT);

    private static final Map<String, GLTFChannelPath> codeToPath;
    private final GLTFComponentType supportedComponentType;
    private final GLTFAttributeType accessorType;

    private String code;

    static {
        codeToPath = Maps.newLinkedHashMap();
        for (GLTFChannelPath path : GLTFChannelPath.values()) {
            codeToPath.put(path.code, path);
        }
    }

    GLTFChannelPath(String code, GLTFAttributeType accessorType, GLTFComponentType supportedComponentType) {
        this.code = code;
        this.accessorType = accessorType;
        this.supportedComponentType = supportedComponentType;
    }

    public String getCode() {
        return code;
    }

    public GLTFAttributeType getAccessorType() {
        return accessorType;
    }

    public GLTFComponentType getSupportedComponentType() {
        return supportedComponentType;
    }

    public static GLTFChannelPath getPathFromCode(String code) {
        return codeToPath.get(code);
    }
}

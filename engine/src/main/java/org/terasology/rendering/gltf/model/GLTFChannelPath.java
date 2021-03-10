// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.gltf.model;

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

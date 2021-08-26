// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.gltf;

import com.google.common.collect.ImmutableSet;
import org.terasology.engine.rendering.gltf.model.GLTFAttributeType;
import org.terasology.engine.rendering.gltf.model.GLTFComponentType;

import java.util.Set;

/**
 * Enumeration of the vertex Attribute Semantics supported by Terasology.
 */
public enum MeshAttributeSemantic {
    Normal("NORMAL", GLTFAttributeType.VEC3, GLTFComponentType.FLOAT),
    Position("POSITION", GLTFAttributeType.VEC3, GLTFComponentType.FLOAT),
    Texcoord_0("TEXCOORD_0", GLTFAttributeType.VEC2, GLTFComponentType.FLOAT),
    Texcoord_1("TEXCOORD_1", GLTFAttributeType.VEC2, GLTFComponentType.FLOAT),
    Color_0("COLOR_0", new GLTFAttributeType[]{GLTFAttributeType.VEC4},
            new GLTFComponentType[]{GLTFComponentType.FLOAT}),
    Joints_0("JOINTS_0", new GLTFAttributeType[]{GLTFAttributeType.VEC4},
            new GLTFComponentType[]{GLTFComponentType.UNSIGNED_BYTE, GLTFComponentType.UNSIGNED_SHORT}),
    Weights_0("WEIGHTS_0", new GLTFAttributeType[]{GLTFAttributeType.VEC4},
            new GLTFComponentType[]{GLTFComponentType.FLOAT});

    private final String name;
    private final Set<GLTFAttributeType> supportedAccessorTypes;
    private final Set<GLTFComponentType> supportedComponentTypes;

    MeshAttributeSemantic(String name, GLTFAttributeType supportedAccessorType, GLTFComponentType supportedComponentType) {
        this(name, new GLTFAttributeType[]{supportedAccessorType}, new GLTFComponentType[]{supportedComponentType});
    }

    MeshAttributeSemantic(String name, GLTFAttributeType[] supportedAccessorTypes, GLTFComponentType[] supportedComponentTypes) {
        this.name = name;
        this.supportedAccessorTypes = ImmutableSet.copyOf(supportedAccessorTypes);
        this.supportedComponentTypes = ImmutableSet.copyOf(supportedComponentTypes);
    }

    /**
     * @return The name of the Attribute Semantic
     */
    public String getName() {
        return name;
    }

    /**
     * @return GLTFAttributeTypes which are supported for this attribute
     */
    public Set<GLTFAttributeType> getSupportedAccessorTypes() {
        return supportedAccessorTypes;
    }

    /**
     * @return GLTFComponentTypes which are supported for this attribute
     */
    public Set<GLTFComponentType> getSupportedComponentTypes() {
        return supportedComponentTypes;
    }

}

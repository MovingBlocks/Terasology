// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.gltf;

import com.google.common.collect.ImmutableSet;
import gnu.trove.list.TFloatList;
import gnu.trove.list.array.TFloatArrayList;
import org.terasology.engine.rendering.assets.mesh.MeshData;
import org.terasology.engine.rendering.assets.mesh.StandardMeshData;
import org.terasology.engine.rendering.gltf.model.GLTFAttributeType;
import org.terasology.engine.rendering.gltf.model.GLTFComponentType;

import java.util.Set;
import java.util.function.Function;

/**
 * Enumeration of the vertex Attribute Semantics supported by Terasology.
 */
public enum MeshAttributeSemantic {
    Normal("NORMAL", GLTFAttributeType.VEC3, GLTFComponentType.FLOAT, k -> k.normals),
    Position("POSITION", GLTFAttributeType.VEC3, GLTFComponentType.FLOAT, MeshData::getVertices),
    Texcoord_0("TEXCOORD_0", GLTFAttributeType.VEC2, GLTFComponentType.FLOAT, k -> k.uv0),
    Texcoord_1("TEXCOORD_1", GLTFAttributeType.VEC2, GLTFComponentType.FLOAT, k -> k.uv1),
    Color_0("COLOR_0", new GLTFAttributeType[]{GLTFAttributeType.VEC4}, new GLTFComponentType[]{GLTFComponentType.FLOAT},  k-> k.color0),
    Joints_0("JOINTS_0", new GLTFAttributeType[]{GLTFAttributeType.VEC4}, new GLTFComponentType[]{GLTFComponentType.UNSIGNED_BYTE, GLTFComponentType.UNSIGNED_SHORT}, x -> new TFloatArrayList()),
    Weights_0("WEIGHTS_0", new GLTFAttributeType[]{GLTFAttributeType.VEC4}, new GLTFComponentType[]{GLTFComponentType.FLOAT}, x -> new TFloatArrayList());

    private final String name;
    private final Set<GLTFAttributeType> supportedAccessorTypes;
    private final Set<GLTFComponentType> supportedComponentTypes;
    private final Function<StandardMeshData, TFloatList> targetBufferSupplier;

    MeshAttributeSemantic(String name, GLTFAttributeType supportedAccessorType, GLTFComponentType supportedComponentType, Function<StandardMeshData, TFloatList> targetBufferSupplier) {
        this(name, new GLTFAttributeType[]{supportedAccessorType}, new GLTFComponentType[]{supportedComponentType}, targetBufferSupplier);
    }

    MeshAttributeSemantic(String name, GLTFAttributeType[] supportedAccessorTypes, GLTFComponentType[] supportedComponentTypes, Function<StandardMeshData, TFloatList> targetBufferSupplier) {
        this.name = name;
        this.supportedAccessorTypes = ImmutableSet.copyOf(supportedAccessorTypes);
        this.supportedComponentTypes = ImmutableSet.copyOf(supportedComponentTypes);
        this.targetBufferSupplier = targetBufferSupplier;
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

    /**
     * @param meshData
     * @return The list of floats from mesh data that corresponds to this attribute
     */
    public TFloatList getTargetFloatBuffer(StandardMeshData meshData) {
        return (TFloatList) targetBufferSupplier.apply(meshData);
    }

}

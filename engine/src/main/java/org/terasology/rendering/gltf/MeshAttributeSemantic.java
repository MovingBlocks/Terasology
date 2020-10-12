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
package org.terasology.rendering.gltf;

import com.google.common.collect.ImmutableSet;
import gnu.trove.list.TFloatList;
import gnu.trove.list.array.TFloatArrayList;
import org.terasology.rendering.assets.mesh.MeshData;
import org.terasology.rendering.gltf.model.GLTFAttributeType;
import org.terasology.rendering.gltf.model.GLTFComponentType;

import java.util.Set;
import java.util.function.Function;

/**
 * Enumeration of the vertex Attribute Semantics supported by Terasology.
 */
public enum MeshAttributeSemantic {
    Normal("NORMAL", GLTFAttributeType.VEC3, GLTFComponentType.FLOAT, MeshData::getNormals),
    Position("POSITION", GLTFAttributeType.VEC3, GLTFComponentType.FLOAT, MeshData::getVertices),
    Texcoord_0("TEXCOORD_0", GLTFAttributeType.VEC2, GLTFComponentType.FLOAT, MeshData::getTexCoord0),
    Texcoord_1("TEXCOORD_1", GLTFAttributeType.VEC2, GLTFComponentType.FLOAT, MeshData::getTexCoord1),
    Color_0("COLOR_0", new GLTFAttributeType[]{GLTFAttributeType.VEC4}, new GLTFComponentType[]{GLTFComponentType.FLOAT}, MeshData::getColors),
    Joints_0("JOINTS_0", new GLTFAttributeType[]{GLTFAttributeType.VEC4}, new GLTFComponentType[]{GLTFComponentType.UNSIGNED_BYTE, GLTFComponentType.UNSIGNED_SHORT}, x -> new TFloatArrayList()),
    Weights_0("WEIGHTS_0", new GLTFAttributeType[]{GLTFAttributeType.VEC4}, new GLTFComponentType[]{GLTFComponentType.FLOAT}, x -> new TFloatArrayList());

    private final String name;
    private final Set<GLTFAttributeType> supportedAccessorTypes;
    private final Set<GLTFComponentType> supportedComponentTypes;
    private final Function<MeshData, TFloatList> targetBufferSupplier;

    MeshAttributeSemantic(String name, GLTFAttributeType supportedAccessorType, GLTFComponentType supportedComponentType, Function<MeshData, TFloatList> targetBufferSupplier) {
        this(name, new GLTFAttributeType[]{supportedAccessorType}, new GLTFComponentType[]{supportedComponentType}, targetBufferSupplier);
    }

    MeshAttributeSemantic(String name, GLTFAttributeType[] supportedAccessorTypes, GLTFComponentType[] supportedComponentTypes, Function<MeshData, TFloatList> targetBufferSupplier) {
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
    public TFloatList getTargetFloatBuffer(MeshData meshData) {
        return (TFloatList) targetBufferSupplier.apply(meshData);
    }

}

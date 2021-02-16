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

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import javax.annotation.Nullable;

/**
 * A glTF node is part of a hierarchy. This can be used to define scenes or mesh and/or cameras, or define a skeleton for a skeletal mesh.
 * See https://github.com/KhronosGroup/glTF/blob/master/specification/2.0/README.md#reference-node for details
 */
public class GLTFNode {

    private String name = "";
    private Integer camera;
    private TIntList children = new TIntArrayList();
    private Integer skin;
    private Matrix4f matrix;
    private Integer mesh;
    private Quaternionf rotation;
    private Vector3f scale;
    private Vector3f translation;
    private TIntList weights = new TIntArrayList();

    /**
     * @return The name of the node
     */
    public String getName() {
        return name;
    }

    /**
     * @return The index of the camera at this node, if any
     */
    @Nullable
    public Integer getCamera() {
        return camera;
    }

    /**
     * @return Indexes of child nodes
     */
    public TIntList getChildren() {
        return children;
    }

    /**
     * @return The transformation matrix for this node
     */
    public Matrix4f getMatrix() {
        return matrix;
    }

    /**
     * @return The rotation of this node
     */
    public Quaternionf getRotation() {
        return rotation;
    }

    /**
     * @return The scale of this node
     */
    public Vector3f getScale() {
        return scale;
    }

    /**
     * @return The translation of this node
     */
    public Vector3f getTranslation() {
        return translation;
    }

    /**
     * @return The index of the mesh at this node, if any
     */
    @Nullable
    public Integer getMesh() {
        return mesh;
    }

    /**
     * @return The index of the skin referenced by this node, if any
     */
    @Nullable
    public Integer getSkin() {
        return skin;
    }

    /**
     * @return The weights of the morph targets
     */
    public TIntList getWeights() {
        return weights;
    }
}

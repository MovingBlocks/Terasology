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

import com.google.common.collect.Lists;

import java.util.List;

/**
 * Root GLTF object. See https://github.com/KhronosGroup/glTF/blob/master/specification/2.0/README.md#reference-gltf for a full description
 */
public class GLTF {

    private GLTFAsset asset = new GLTFAsset();
    private List<GLTFScene> scenes = Lists.newArrayList();
    private int scene;
    private List<GLTFNode> nodes = Lists.newArrayList();
    private List<GLTFBuffer> buffers = Lists.newArrayList();
    private List<GLTFBufferView> bufferViews = Lists.newArrayList();
    private List<GLTFAccessor> accessors = Lists.newArrayList();
    private List<GLTFMesh> meshes = Lists.newArrayList();
    private List<GLTFSkin> skins = Lists.newArrayList();
    private List<GLTFAnimation> animations = Lists.newArrayList();

    /**
     * @return Metadata on the GLTF asset
     */
    public GLTFAsset getAsset() {
        return asset;
    }

    /**
     * @return A list of scenes present in the asset
     */
    public List<GLTFScene> getScenes() {
        return scenes;
    }

    /**
     * @return Index of the default scene
     */
    public int getScene() {
        return scene;
    }

    /**
     * @return A list of the nodes in the asset
     */
    public List<GLTFNode> getNodes() {
        return nodes;
    }

    /**
     * @return A list of the buffers in the asset
     */
    public List<GLTFBuffer> getBuffers() {
        return buffers;
    }

    /**
     * @return A list of the buffer views in the asset
     */
    public List<GLTFBufferView> getBufferViews() {
        return bufferViews;
    }

    /**
     * @return A list of the accessors in the asset
     */
    public List<GLTFAccessor> getAccessors() {
        return accessors;
    }

    /**
     * @return A list of the meshes in the asset
     */
    public List<GLTFMesh> getMeshes() {
        return meshes;
    }

    /**
     * @return A list of skins in the asset
     */
    public List<GLTFSkin> getSkins() {
        return skins;
    }

    /**
     * @return A list of animations in the asset
     */
    public List<GLTFAnimation> getAnimations() {
        return animations;
    }
}

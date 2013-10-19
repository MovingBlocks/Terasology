/*
 * Copyright 2013 MovingBlocks
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
package org.terasology.rendering.assets.mesh;

import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.asset.Assets;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

/**
 * @author Immortius
 */
public class MeshBuilder {
    private MeshData meshData = new MeshData();
    private int vertexCount;

    public MeshBuilder addVertex(Vector3f v) {
        meshData.getVertices().add(v.x);
        meshData.getVertices().add(v.y);
        meshData.getVertices().add(v.z);
        vertexCount++;
        return this;
    }

    public MeshBuilder addPoly(Vector3f v1, Vector3f v2, Vector3f v3, Vector3f ... vn) {
        for (int i = 0; i < vn.length + 1; i++) {
            addIndices(vertexCount, vertexCount + i + 2, vertexCount + i + 1);
        }
        addVertex(v1);
        addVertex(v2);
        addVertex(v3);
        for (Vector3f v : vn) {
            addVertex(v);
        }
        return this;
    }

    public MeshBuilder addTexCoord(float x, float y) {
        meshData.getTexCoord0().add(x);
        meshData.getTexCoord0().add(y);
        return this;
    }

    public MeshBuilder addTexCoord(Vector2f v) {
        return addTexCoord(v.x, v.y);
    }

    public MeshBuilder addIndex(int index) {
        meshData.getIndices().add(index);
        return this;
    }

    public MeshBuilder addIndices(int ... indices) {
        meshData.getIndices().add(indices);
        return this;
    }

    public MeshData getMeshData() {
        return meshData;
    }

    public Mesh build() {
        return Assets.generateAsset(AssetType.MESH, meshData, Mesh.class);
    }

    public Mesh build(AssetUri uri) {
        return Assets.generateAsset(uri, meshData, Mesh.class);
    }
}

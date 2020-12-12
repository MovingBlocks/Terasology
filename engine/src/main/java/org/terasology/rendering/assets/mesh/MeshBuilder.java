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

import org.joml.Vector2fc;
import org.joml.Vector3fc;
import org.terasology.assets.ResourceUrn;
import org.terasology.math.geom.Vector2f;
import org.terasology.math.geom.Vector3f;
import org.terasology.module.sandbox.API;
import org.terasology.nui.Colorc;
import org.terasology.utilities.Assets;

public class MeshBuilder {
    private static final float[] VERTICES = {
            // Front face
            0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 1.0f,
            1.0f, 1.0f, 1.0f,
            0.0f, 1.0f, 1.0f,

            // Back face
            0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f,
            1.0f, 1.0f, 0.0f,
            1.0f, 0.0f, 0.0f,

            // Top face
            0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 0.0f,

            // Bottom face
            0.0f, 0.0f, 0.0f,
            1.0f, 0.0f, 0.0f,
            1.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f,

            // Right face
            1.0f, 0.0f, 0.0f,
            1.0f, 1.0f, 0.0f,
            1.0f, 1.0f, 1.0f,
            1.0f, 0.0f, 1.0f,

            // Left face
            0.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 1.0f,
            0.0f, 1.0f, 1.0f,
            0.0f, 1.0f, 0.0f
    };

    private static final int[] INDICES = {
            0, 1, 2, 0, 2, 3,    // front
            4, 5, 6, 4, 6, 7,    // back
            8, 9, 10, 8, 10, 11,   // top
            12, 13, 14, 12, 14, 15,   // bottom
            16, 17, 18, 16, 18, 19,   // right
            20, 21, 22, 20, 22, 23    // left
    };

    private MeshData meshData = new MeshData();
    private int vertexCount;
    private TextureMapper textureMapper;

    /**
     *
     * @param v
     * @return
     * @deprecated This is scheduled for removal in an upcoming version
     *      method will be replaced with JOML implementation {@link #addVertex(Vector3fc)}.
     */
    public MeshBuilder addVertex(Vector3f v) {
        meshData.getVertices().add(v.x);
        meshData.getVertices().add(v.y);
        meshData.getVertices().add(v.z);
        vertexCount++;
        return this;
    }

    public MeshBuilder addVertex(Vector3fc v) {
        meshData.getVertices().add(v.x());
        meshData.getVertices().add(v.y());
        meshData.getVertices().add(v.z());
        vertexCount++;
        return this;
    }

    /**
     *
     * @param v1
     * @param v2
     * @param v3
     * @param vn
     * @return
     * @deprecated This is scheduled for removal in an upcoming version
     *      method will be replaced with JOML implementation {@link #addPoly(Vector3fc, Vector3fc, Vector3fc, Vector3fc...)}.
     */
    public MeshBuilder addPoly(Vector3f v1, Vector3f v2, Vector3f v3, Vector3f... vn) {
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

    /**
     *
     * @param v1
     * @param v2
     * @param v3
     * @param vn
     * @return
     */
    public MeshBuilder addPoly(Vector3fc v1, Vector3fc v2, Vector3fc v3, Vector3fc... vn) {
        for (int i = 0; i < vn.length + 1; i++) {
            addIndices(vertexCount, vertexCount + i + 2, vertexCount + i + 1);
        }
        addVertex(v1);
        addVertex(v2);
        addVertex(v3);
        for (Vector3fc v : vn) {
            addVertex(v);
        }
        return this;
    }

    public MeshBuilder addColor(Colorc c1, Colorc... colors) {
        meshData.getColors().add(c1.rf());
        meshData.getColors().add(c1.gf());
        meshData.getColors().add(c1.bf());
        meshData.getColors().add(c1.af());
        for (Colorc c : colors) {
            meshData.getColors().add(c.rf());
            meshData.getColors().add(c.gf());
            meshData.getColors().add(c.bf());
            meshData.getColors().add(c.af());
        }
        return this;
    }

    public MeshBuilder addTexCoord(float x, float y) {
        meshData.getTexCoord0().add(x);
        meshData.getTexCoord0().add(y);
        return this;
    }

    /**
     *
     * @param v
     * @return
     *
     * @deprecated This is scheduled for removal in an upcoming version
     *      method will be replaced with JOML implementation {@link #addTexCoord(Vector2fc)}.
     */
    public MeshBuilder addTexCoord(Vector2f v) {
        return addTexCoord(v.x, v.y);
    }

    public MeshBuilder addTexCoord(Vector2fc v) {
        return addTexCoord(v.x(), v.y());
    }

    public MeshBuilder addIndex(int index) {
        meshData.getIndices().add(index);
        return this;
    }

    public MeshBuilder addIndices(int... indices) {
        meshData.getIndices().add(indices);
        return this;
    }

    public MeshData getMeshData() {
        return meshData;
    }

    public Mesh build() {
        return Assets.generateAsset(meshData, Mesh.class);
    }

    public Mesh build(ResourceUrn urn) {
        return Assets.generateAsset(urn, meshData, Mesh.class);
    }

    /**
     * Add vertices, texture coordinate and indices for a box specified by offset and size.
     * <br><br>
     * Use the texture mapper to change how texture coordinates (u and v) are applied to each vertex.
     */
    public MeshBuilder addBox(org.joml.Vector3f offset, org.joml.Vector3f size, float u, float v) {
        int vertexId = vertexCount;
        textureMapper.initialize(offset, size);
        for (int i = 0; i < VERTICES.length / 3; i++) {
            addVertex(new Vector3f(offset.x + size.x * VERTICES[i * 3], offset.y + size.y * VERTICES[i * 3 + 1], offset.z + size.z * VERTICES[i * 3 + 2]));
            addTexCoord(textureMapper.map(i, u, v));
        }
        for (int i : INDICES) {
            addIndex(vertexId + i);
        }
        return this;
    }

    public void setTextureMapper(TextureMapper textureMapper) {
        this.textureMapper = textureMapper;
    }

    @API
    public interface TextureMapper {
        void initialize(org.joml.Vector3f offset, org.joml.Vector3f size);

        org.joml.Vector2fc map(int vertexIndex, float u, float v);
    }
}

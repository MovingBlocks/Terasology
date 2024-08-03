// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.assets.mesh;

import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.context.annotation.API;
import org.terasology.nui.Colorc;
import org.terasology.engine.utilities.Assets;

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

    private int vertexCount;
    private TextureMapper textureMapper;
    private final StandardMeshData meshData = new StandardMeshData();

    public MeshBuilder addVertex(Vector3fc v) {
        meshData.position.put(v);
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
        meshData.color0.put(c1);
        for (Colorc c : colors) {
            meshData.color0.put(c);
        }
        return this;
    }

    public MeshBuilder addTexCoord(float x, float y) {
        meshData.uv0.put(new Vector2f(x, y));
        return this;
    }

    public MeshBuilder addTexCoord(Vector2fc v) {
        return addTexCoord(v.x(), v.y());
    }

    public MeshBuilder addIndex(int index) {
        meshData.indices.put(index);
        return this;
    }

    public MeshBuilder addIndices(int... indices) {
        for (int index : indices) {
            meshData.indices.put(index);
        }
        return this;
    }

    public StandardMeshData buildMeshData() {
        return new StandardMeshData(meshData);
    }

    public Mesh build() {
        return Assets.generateAsset(buildMeshData(), Mesh.class);
    }

    public Mesh build(ResourceUrn urn) {
        return Assets.generateAsset(urn, buildMeshData(), Mesh.class);
    }

    /**
     * Add vertices, texture coordinate and indices for a box specified by offset and size.
     * <br><br>
     * Use the texture mapper to change how texture coordinates (u and v) are applied to each vertex.
     */
    public MeshBuilder addBox(Vector3fc offset, Vector3fc size, float u, float v) {
        int vertexId = vertexCount;
        textureMapper.initialize(offset, size);
        for (int i = 0; i < VERTICES.length / 3; i++) {
            addVertex(new Vector3f(offset.x() + size.x() * VERTICES[i * 3],
                    offset.y() + size.y() * VERTICES[i * 3 + 1],
                    offset.z() + size.z() * VERTICES[i * 3 + 2]));
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
        void initialize(Vector3fc offset, Vector3fc size);

        Vector2fc map(int vertexIndex, float u, float v);
    }
}

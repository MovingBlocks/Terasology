// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.assets.mesh;

import gnu.trove.list.TFloatList;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TFloatArrayList;
import gnu.trove.list.array.TIntArrayList;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.module.sandbox.API;
import org.terasology.nui.Color;
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

    private final TFloatList position = new TFloatArrayList();
    private final TFloatList color0 = new TFloatArrayList();
    public final TFloatList uv0 = new TFloatArrayList();
    public final TIntList indices = new TIntArrayList();


    private int vertexCount;
    private TextureMapper textureMapper;

    public MeshBuilder addVertex(Vector3fc v) {
        position.add(v.x());
        position.add(v.y());
        position.add(v.z());
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
        color0.add(c1.rf());
        color0.add(c1.gf());
        color0.add(c1.bf());
        color0.add(c1.af());

        for (Colorc c : colors) {
            color0.add(c.rf());
            color0.add(c.gf());
            color0.add(c.bf());
            color0.add(c.af());
        }
        return this;
    }

    public MeshBuilder addTexCoord(float x, float y) {
        uv0.add(x);
        uv0.add(y);
        return this;
    }

    public MeshBuilder addTexCoord(Vector2fc v) {
        return addTexCoord(v.x(), v.y());
    }

    public MeshBuilder addIndex(int index) {
        indices.add(index);
        return this;
    }

    public MeshBuilder addIndices(int... indices) {
        for (int x = 0; x < indices.length; x++) {
            this.indices.add(indices[x]);
        }
        return this;
    }

    public StandardMeshData buildMeshData() {
        StandardMeshData meshData = new StandardMeshData();
        Vector3f pos = new Vector3f();
        for (int x = 0; x < ((this.position.size() / 3) * 3); x += 3) {
            pos.set(this.position.get(x), this.position.get(x + 1), this.position.get(x + 2));
            meshData.position.put(pos);
        }
        Color color = new Color();
        Vector4f c = new Vector4f();
        for (int x = 0; x < ((this.color0.size() / 4) * 4); x += 4) {
            color.set((int) (this.color0.get(x) * 255.0f), (int) (this.color0.get(x + 1) * 255.0f), (int) (this.color0.get(x + 2) * 255.0f), (int) (this.color0.get(x + 3) * 255.0f));
            meshData.color0.put(color);
        }
        Vector2f uv = new Vector2f();
        for (int x = 0; x < ((this.uv0.size() / 2) * 2); x += 2) {
            uv.set(this.uv0.get(x), this.uv0.get(x + 1));
            meshData.uv0.put(uv);
        }
        for (int x = 0; x < indices.size(); x++) {
            meshData.indices.put(indices.get(x));
        }
        return meshData;
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
            addVertex(new Vector3f(offset.x() + size.x() * VERTICES[i * 3], offset.y() + size.y() * VERTICES[i * 3 + 1], offset.z() + size.z() * VERTICES[i * 3 + 2]));
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

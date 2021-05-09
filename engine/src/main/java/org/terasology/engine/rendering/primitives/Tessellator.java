// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.primitives;

import com.google.common.base.Preconditions;
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
import org.terasology.engine.rendering.assets.mesh.StandardMeshData;
import org.terasology.gestalt.module.sandbox.API;
import org.terasology.engine.rendering.assets.mesh.Mesh;
import org.terasology.engine.rendering.assets.mesh.MeshData;
import org.terasology.engine.utilities.Assets;
import org.terasology.engine.world.block.shapes.BlockMeshPart;
import org.terasology.nui.Color;

@API
public class Tessellator {

    private int nextIndex;

    private Vector4f activeColor = new Vector4f();
    private Vector3f activeNormal = new Vector3f();
    private Vector2f activeTex = new Vector2f();
    private Vector3f lighting = new Vector3f();

    private boolean useLighting = true;
    private boolean useNormals = true;

    public final TFloatList positions = new TFloatArrayList();
    public final TFloatList uv0 = new TFloatArrayList();
    public final TFloatList normals = new TFloatArrayList();
    public final TFloatList color0 = new TFloatArrayList();
    public final TFloatList light0 = new TFloatArrayList();
    public final TIntList indices = new TIntArrayList();

    public Tessellator() {
        resetParams();
    }

    public void setUseLighting(boolean enable) {
        this.useLighting = enable;
    }

    public void setUseNormals(boolean enable) {
        this.useNormals = enable;
    }

    public void resetParams() {
        activeColor.set(1, 1, 1, 1);
        activeTex.set(0, 0);
        lighting.set(1, 1, 1);
        activeNormal.set(0, 1, 0);
    }



    public void addPoly(Vector3f[] vertices, Vector2f[] texCoords) {
        if (vertices.length != texCoords.length || vertices.length < 3) {
            throw new IllegalArgumentException("addPoly expected vertices.length == texCoords.length > 2");
        }
        for (int i = 0; i < vertices.length; ++i) {
            positions.add(vertices[i].x);
            positions.add(vertices[i].y);
            positions.add(vertices[i].z);

            color0.add(activeColor.x);
            color0.add(activeColor.y);
            color0.add(activeColor.z);
            color0.add(activeColor.w);

            if (useNormals) {
                normals.add(activeNormal.x);
                normals.add(activeNormal.y);
                normals.add(activeNormal.z);
            }

            uv0.add(texCoords[i].x);
            uv0.add(texCoords[i].y);

            if (useLighting) {
                light0.add(lighting.x);
                light0.add(lighting.y);
                light0.add(lighting.z);
            }
        }

        // Standard fan
        for (int i = 0; i < vertices.length - 2; i++) {
            indices.add(nextIndex);
            indices.add(nextIndex + i + 1);
            indices.add(nextIndex + i + 2);
        }
        nextIndex += vertices.length;
    }

    public void addMeshPartDoubleSided(BlockMeshPart part) {
        addMeshPart(part, true);
    }

    public void addMeshPart(BlockMeshPart part) {
        addMeshPart(part, false);
    }

    private void addMeshPart(BlockMeshPart part, boolean doubleSided) {
        for (int i = 0; i < part.size(); ++i) {
            Vector3fc vertex = part.getVertex(i);
            positions.add(vertex.x());
            positions.add(vertex.y());
            positions.add(vertex.z());

            color0.add(activeColor.x);
            color0.add(activeColor.y);
            color0.add(activeColor.z);
            color0.add(activeColor.w);

            Vector3fc normal = part.getNormal(i);
            normals.add(normal.x());
            normals.add(normal.y());
            normals.add(normal.z());

            Vector2fc uv = part.getTexCoord(i);
            uv0.add(uv.x());
            uv0.add(uv.y());

            light0.add(lighting.x);
            light0.add(lighting.y);
            light0.add(lighting.z);
        }

        for (int i = 0; i < part.indicesSize(); ++i) {
            indices.add(nextIndex + part.getIndex(i));
        }
        if (doubleSided) {
            for (int i = 0; i < part.indicesSize(); i += 3) {
                int i1 = nextIndex + part.getIndex(i);
                int i2 = nextIndex + part.getIndex(i + 1);
                int i3 = nextIndex + part.getIndex(i + 2);
                indices.add(i1);
                indices.add(i3);
                indices.add(i2);
            }
        }

        nextIndex += part.size();
    }

    public void setColor(Vector4f v) {
        activeColor.set(v);
    }

    public void setNormal(Vector3f v) {
        activeNormal.set(v);
    }

    public void setTex(Vector2f v) {
        activeTex.set(v);
    }

    public void setLighting(Vector3f v) {
        lighting.set(v);
    }

    public MeshData generateMeshData() {
        StandardMeshData meshData = new StandardMeshData();
        Vector3f pos = new Vector3f();
        for (int x = 0; x < ((this.positions.size() / 3) * 3); x += 3) {
            pos.set(this.positions.get(x), this.positions.get(x + 1), this.positions.get(x + 2));
            meshData.position.put(pos);
        }
        Color color = new Color();
        for (int x = 0; x < ((this.color0.size() / 4) * 4); x += 4) {
            color.set((int) (this.color0.get(x) * 255.0f), (int) (this.color0.get(x + 1) * 255.0f),
                    (int) (this.color0.get(x + 2) * 255.0f), (int) (this.color0.get(x + 3) * 255.0f));
            meshData.color0.put(color);
        }
        for (int x = 0; x < ((this.light0.size() / 3) * 3); x += 3) {
            pos.set(this.light0.get(x), this.light0.get(x + 1), this.light0.get(x + 2));
            meshData.light0.put(pos);
        }
        for (int x = 0; x < ((this.normals.size() / 3) * 3); x += 3) {
            pos.set(this.normals.get(x), this.normals.get(x + 1), this.normals.get(x + 2));
            meshData.normal.put(pos);
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

    public Mesh generateMesh(ResourceUrn urn) {
        Preconditions.checkNotNull(urn);
        return Assets.generateAsset(urn, generateMeshData(), Mesh.class);
    }

    public Mesh generateMesh() {
        return Assets.generateAsset(generateMeshData(), Mesh.class);
    }
}

// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.primitives;

import com.google.common.base.Preconditions;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.engine.rendering.assets.mesh.StandardMeshData;
import org.terasology.context.annotation.API;
import org.terasology.engine.rendering.assets.mesh.Mesh;
import org.terasology.engine.rendering.assets.mesh.MeshData;
import org.terasology.engine.utilities.Assets;
import org.terasology.engine.world.block.shapes.BlockMeshPart;
import org.terasology.nui.Color;

@API
public class Tessellator {

    private int nextIndex;

    private Color activeColor = new Color();
    private Vector3f activeNormal = new Vector3f();
    private Vector2f activeTex = new Vector2f();
    private Vector3f lighting = new Vector3f();

    private boolean useLighting = true;
    private boolean useNormals = true;
    private StandardMeshData meshData = new StandardMeshData();

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
        activeColor.set(255, 255, 255, 255);
        activeTex.set(0, 0);
        lighting.set(1, 1, 1);
        activeNormal.set(0, 1, 0);
    }



    public void addPoly(Vector3f[] vertices, Vector2f[] texCoords) {
        if (vertices.length != texCoords.length || vertices.length < 3) {
            throw new IllegalArgumentException("addPoly expected vertices.length == texCoords.length > 2");
        }
        for (int i = 0; i < vertices.length; ++i) {
            meshData.position.put(vertices[i]);
            meshData.color0.put(activeColor);

            if (useNormals) {
                meshData.normal.put(activeNormal);
            }
            meshData.uv0.put(texCoords[i]);
            if (useLighting) {
                meshData.light0.put(lighting);
            }
        }

        // Standard fan
        for (int i = 0; i < vertices.length - 2; i++) {
            meshData.indices.put(nextIndex);
            meshData.indices.put(nextIndex + i + 1);
            meshData.indices.put(nextIndex + i + 2);
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
            meshData.position.put(vertex);
            meshData.color0.put(activeColor);
            meshData.normal.put(part.getNormal(i));
            meshData.uv0.put(part.getTexCoord(i));
            meshData.light0.put(lighting);
        }
        for (int i = 0; i < part.indicesSize(); ++i) {
            meshData.indices.put(nextIndex + part.getIndex(i));
        }
        if (doubleSided) {
            for (int i = 0; i < part.indicesSize(); i += 3) {
                meshData.indices.put(nextIndex + part.getIndex(i + 1));
                meshData.indices.put(nextIndex + part.getIndex(i));
                meshData.indices.put(nextIndex + part.getIndex(i + 2));
            }
        }

        nextIndex += part.size();
    }

    public void setColor(Vector4f v) {
        activeColor.set((int) (v.x() * 255.0f), (int) (v.y() * 255.0f), (int) (v.z() * 255.0f), (int) (v.w() * 255.0f));
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

    public MeshData buildMeshData() {
        return new StandardMeshData(meshData);
    }

    public Mesh generateMesh(ResourceUrn urn) {
        Preconditions.checkNotNull(urn);
        return Assets.generateAsset(urn, buildMeshData(), Mesh.class);
    }

    public Mesh generateMesh() {
        return Assets.generateAsset(buildMeshData(), Mesh.class);
    }
}

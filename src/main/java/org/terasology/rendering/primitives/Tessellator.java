/*
 * Copyright 2013 Moving Blocks
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
package org.terasology.rendering.primitives;

import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.asset.Assets;
import org.terasology.rendering.assets.mesh.Mesh;
import org.terasology.rendering.assets.mesh.MeshData;
import org.terasology.world.block.shapes.BlockMeshPart;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

public class Tessellator {

    private MeshData meshData = new MeshData();

    private int nextIndex = 0;

    private Vector4f activeColor = new Vector4f();
    private Vector3f activeNormal = new Vector3f();
    private Vector2f activeTex = new Vector2f();
    private Vector3f lighting = new Vector3f();

    private boolean useLighting = true;
    private boolean useNormals = true;


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
            meshData.getVertices().add(vertices[i].x);
            meshData.getVertices().add(vertices[i].y);
            meshData.getVertices().add(vertices[i].z);

            meshData.getColors().add(activeColor.x);
            meshData.getColors().add(activeColor.y);
            meshData.getColors().add(activeColor.z);
            meshData.getColors().add(activeColor.w);

            if (useNormals) {
                meshData.getNormals().add(activeNormal.x);
                meshData.getNormals().add(activeNormal.y);
                meshData.getNormals().add(activeNormal.z);
            }

            meshData.getTexCoord0().add(texCoords[i].x);
            meshData.getTexCoord0().add(texCoords[i].y);

            if (useLighting) {
                meshData.getTexCoord1().add(lighting.x);
                meshData.getTexCoord1().add(lighting.y);
                meshData.getTexCoord1().add(lighting.z);
            }
        }

        // Standard fan
        for (int i = 0; i < vertices.length - 2; i++) {
            meshData.getIndices().add(nextIndex);
            meshData.getIndices().add(nextIndex + i + 1);
            meshData.getIndices().add(nextIndex + i + 2);
        }
        nextIndex += vertices.length;
    }

    public void addMeshPart(BlockMeshPart part) {
        for (int i = 0; i < part.size(); ++i) {
            Vector3f vertex = part.getVertex(i);
            meshData.getVertices().add(vertex.x);
            meshData.getVertices().add(vertex.y);
            meshData.getVertices().add(vertex.z);

            meshData.getColors().add(activeColor.x);
            meshData.getColors().add(activeColor.y);
            meshData.getColors().add(activeColor.z);
            meshData.getColors().add(activeColor.w);

            Vector3f normal = part.getNormal(i);
            meshData.getNormals().add(normal.x);
            meshData.getNormals().add(normal.y);
            meshData.getNormals().add(normal.z);

            Vector2f uv = part.getTexCoord(i);
            meshData.getTexCoord0().add(uv.x);
            meshData.getTexCoord0().add(uv.y);

            meshData.getTexCoord1().add(lighting.x);
            meshData.getTexCoord1().add(lighting.y);
            meshData.getTexCoord1().add(lighting.z);
        }

        for (int i = 0; i < part.indicesSize(); ++i) {
            meshData.getIndices().add(nextIndex + part.getIndex(i));
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

    public Mesh generateMesh(AssetUri uri) {
        Mesh result =  Assets.generateAsset(uri, meshData, Mesh.class);
        meshData = new MeshData();
        return result;
    }

    public Mesh generateMesh() {
        Mesh result = Assets.generateAsset(AssetType.MESH, meshData, Mesh.class);
        meshData = new MeshData();
        return result;
    }
}
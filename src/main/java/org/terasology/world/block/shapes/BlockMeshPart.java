/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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
package org.terasology.world.block.shapes;

import java.util.Arrays;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import org.terasology.rendering.primitives.ChunkMesh;

import com.bulletphysics.linearmath.QuaternionUtil;

/**
 * Describes the elements composing part of a block mesh. Multiple parts are patched together to define the mesh
 * for a block, or its appearance in the world.
 *
 * @author Immortius <immortius@gmail.com>
 */
public class BlockMeshPart {
    private Vector3f[] vertices;
    private Vector3f[] normals;
    private Vector2f[] texCoords;
    private int[] indices;

    public BlockMeshPart(Vector3f[] vertices, Vector3f[] normals, Vector2f[] texCoords, int[] indices) {
        this.vertices = Arrays.copyOf(vertices, vertices.length);
        this.normals = Arrays.copyOf(normals, normals.length);
        this.texCoords = Arrays.copyOf(texCoords, texCoords.length);
        this.indices = Arrays.copyOf(indices, indices.length);
    }

    public int size() {
        return vertices.length;
    }

    public int indicesSize() {
        return indices.length;
    }

    public Vector3f getVertex(int i) {
        return vertices[i];
    }

    public Vector3f getNormal(int i) {
        return normals[i];
    }

    public Vector2f getTexCoord(int i) {
        return texCoords[i];
    }

    public int getIndex(int i) {
        return indices[i];
    }

    public BlockMeshPart mapTexCoords(Vector2f offset, float width) {
        Vector2f[] newTexCoords = new Vector2f[texCoords.length];
        for (int i = 0; i < newTexCoords.length; ++i) {
            newTexCoords[i] = new Vector2f(offset.x + texCoords[i].x * width, offset.y + texCoords[i].y * width);
        }
        return new BlockMeshPart(vertices, normals, newTexCoords, indices);
    }

    public void appendTo(ChunkMesh chunk, int offsetX, int offsetY, int offsetZ, Vector4f colorOffset, int meshBit, int flags) {
        for (Vector2f texCoord : texCoords) {
            chunk._vertexElements[meshBit].tex.add(texCoord.x);
            chunk._vertexElements[meshBit].tex.add(texCoord.y);
        }

        int nextIndex = chunk._vertexElements[meshBit].vertCount;
        for (int vIdx = 0; vIdx < vertices.length; ++vIdx) {
            chunk._vertexElements[meshBit].color.add(colorOffset.x);
            chunk._vertexElements[meshBit].color.add(colorOffset.y);
            chunk._vertexElements[meshBit].color.add(colorOffset.z);
            chunk._vertexElements[meshBit].color.add(colorOffset.w);
            chunk._vertexElements[meshBit].vertices.add(vertices[vIdx].x + offsetX);
            chunk._vertexElements[meshBit].vertices.add(vertices[vIdx].y + offsetY);
            chunk._vertexElements[meshBit].vertices.add(vertices[vIdx].z + offsetZ);
            chunk._vertexElements[meshBit].normals.add(normals[vIdx].x);
            chunk._vertexElements[meshBit].normals.add(normals[vIdx].y);
            chunk._vertexElements[meshBit].normals.add(normals[vIdx].z);
            chunk._vertexElements[meshBit].flags.add(flags);
        }
        chunk._vertexElements[meshBit].vertCount += vertices.length;

        for (int i = 0; i < indices.length; ++i) {
            chunk._vertexElements[meshBit].indices.add(indices[i] + nextIndex);
        }
    }

    public BlockMeshPart rotate(Quat4f rotation) {
        Vector3f[] newVertices = new Vector3f[vertices.length];
        Vector3f[] newNormals = new Vector3f[normals.length];

        for (int i = 0; i < newVertices.length; ++i) {
            newVertices[i] = QuaternionUtil.quatRotate(rotation, vertices[i], new Vector3f());
            newNormals[i] = QuaternionUtil.quatRotate(rotation, normals[i], new Vector3f());
            newNormals[i].normalize();
        }

        return new BlockMeshPart(newVertices, newNormals, texCoords, indices);
    }
}

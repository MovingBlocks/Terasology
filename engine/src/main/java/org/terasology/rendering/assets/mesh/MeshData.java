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

import gnu.trove.list.TFloatList;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TFloatArrayList;
import gnu.trove.list.array.TIntArrayList;
import org.joml.AABBf;
import org.terasology.assets.AssetData;

/**
 */
public class MeshData implements AssetData {

    private TFloatList vertices = new TFloatArrayList();
    private TFloatList texCoord0 = new TFloatArrayList();
    private TFloatList texCoord1 = new TFloatArrayList();
    private TFloatList normals = new TFloatArrayList();
    private TFloatList colors = new TFloatArrayList();
    private TIntList indices = new TIntArrayList();

    public MeshData() {
    }

    public TFloatList getVertices() {
        return vertices;
    }

    public AABBf getBounds() {
        int vertexCount = vertices.size() / 3;
        AABBf bounds = new AABBf();
        if (vertexCount == 0) {
            return bounds;
        }
        for (int index = 0; index < vertexCount; index++) {
            bounds.union(vertices.get(3 * index), vertices.get(3 * index + 1), vertices.get(3 * index + 2));
        }
        return bounds;
    }

    public TFloatList getTexCoord0() {
        return texCoord0;
    }

    public TFloatList getTexCoord1() {
        return texCoord1;
    }

    public TFloatList getNormals() {
        return normals;
    }

    public TFloatList getColors() {
        return colors;
    }

    public TIntList getIndices() {
        return indices;
    }
}

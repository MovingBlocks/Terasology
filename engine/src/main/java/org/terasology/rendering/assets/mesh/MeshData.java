// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.assets.mesh;

import gnu.trove.list.TFloatList;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TFloatArrayList;
import gnu.trove.list.array.TIntArrayList;
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

// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.assets.mesh;

import gnu.trove.list.TFloatList;
import gnu.trove.list.array.TFloatArrayList;
import org.terasology.engine.rendering.assets.mesh.layout.FloatLayout;

public class StandardMeshData extends MeshData {
    public final TFloatList vertices;
    public final TFloatList uv0;
    public final TFloatList uv1;
    public final TFloatList normals;
    public final TFloatList color0;
    public final TFloatList light0;

    public StandardMeshData() {
        this.vertices = new TFloatArrayList();
        this.uv0 = new TFloatArrayList();
        this.uv1 = new TFloatArrayList();
        this.normals = new TFloatArrayList();
        this.color0 = new TFloatArrayList();
        this.light0 = new TFloatArrayList();

        addLayout(new FloatLayout(0, 3, vertices));
        addLayout(new FloatLayout(1, 3, normals));
        addLayout(new FloatLayout(2, 2, uv0));
        addLayout(new FloatLayout(3, 2, uv1));
        addLayout(new FloatLayout(4, 4, color0));
        addLayout(new FloatLayout(5, 3, light0));
    }


    @Override
    public TFloatList getVertices() {
        return vertices;
    }

    @Override
    public int getSize() {
        return (this.vertices.size() / 3);
    }
}

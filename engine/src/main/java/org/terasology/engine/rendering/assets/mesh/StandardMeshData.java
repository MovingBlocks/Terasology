// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.assets.mesh;

import gnu.trove.list.TFloatList;
import gnu.trove.list.array.TFloatArrayList;
import org.terasology.engine.rendering.assets.mesh.layout.FloatLayout;
import org.terasology.engine.rendering.assets.mesh.layout.Layout;

public class StandardMeshData extends MeshData {
    public static final int VERTEX_INDEX = 0;
    public static final int NORMAL_INDEX = 1;
    public static final int UV0_INDEX = 2;
    public static final int UV1_INDEX = 3;
    public static final int COLOR0_INDEX = 4;
    public static final int LIGHT0_INDEX = 5;


    public final TFloatList vertices;
    public final TFloatList uv0;
    public final TFloatList uv1;
    public final TFloatList normals;
    public final TFloatList color0;
    public final TFloatList light0;

    public StandardMeshData(DrawingMode mode) {
       super(mode);
        this.vertices = new TFloatArrayList();
        this.uv0 = new TFloatArrayList();
        this.uv1 = new TFloatArrayList();
        this.normals = new TFloatArrayList();
        this.color0 = new TFloatArrayList();
        this.light0 = new TFloatArrayList();

        addLayout(new FloatLayout(VERTEX_INDEX, 3, vertices, Layout.FLOATING_POINT));
        addLayout(new FloatLayout(NORMAL_INDEX, 3, normals, Layout.FLOATING_POINT));
        addLayout(new FloatLayout(UV0_INDEX, 2, uv0, Layout.FLOATING_POINT));
        addLayout(new FloatLayout(UV1_INDEX, 2, uv1, Layout.FLOATING_POINT));
        addLayout(new FloatLayout(COLOR0_INDEX, 4, color0, Layout.FLOATING_POINT));
        addLayout(new FloatLayout(LIGHT0_INDEX, 3, light0, Layout.FLOATING_POINT));

    }
    public StandardMeshData() {
        this(DrawingMode.TRIANGLES);

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

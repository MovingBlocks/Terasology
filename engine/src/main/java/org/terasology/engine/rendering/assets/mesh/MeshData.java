// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.assets.mesh;

import gnu.trove.list.TFloatList;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import org.terasology.assets.AssetData;
import org.terasology.engine.rendering.assets.mesh.layout.Layout;

import java.util.ArrayList;
import java.util.List;

/**
 */
public abstract class MeshData implements AssetData {

    enum DrawingMode {
        POINTS,
        LINES,
        LINE_LOOP,
        LINE_STRIP,
        TRIANGLES,
        TRIANGLE_STRIP,
        TRIANGLE_FAN,
        LINES_ADJACENCY,
        LINE_STRIP_ADJACENCY,
        TRIANGLES_ADJACENCY,
        TRIANGLE_STRIP_ADJACENCY
    };

    private List<Layout> layouts = new ArrayList<>();
    private TIntList indices = new TIntArrayList();

    public List<Layout> getLayouts() {
        return this.layouts;
    }

    public TIntList getIndices() {
        return indices;
    }

    public MeshData addLayout(Layout layout) {
        this.getLayouts().add(layout);
        return this;
    }

    public abstract TFloatList getVertices();
    public abstract int getSize();

}

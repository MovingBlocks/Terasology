// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.assets.atlas;


import org.joml.Vector2i;

import java.util.List;

public class GridDefinition {

    private Vector2i tileSize;
    private Vector2i gridDimensions;
    private Vector2i gridOffset;
    private List<String> tileNames;

    public Vector2i getTileSize() {
        return tileSize;
    }

    public void setTileSize(Vector2i tileSize) {
        this.tileSize = tileSize;
    }

    public Vector2i getGridDimensions() {
        return gridDimensions;
    }

    public void setGridDimensions(Vector2i gridDimensions) {
        this.gridDimensions = gridDimensions;
    }

    public Vector2i getGridOffset() {
        return gridOffset;
    }

    public void setGridOffset(Vector2i gridOffset) {
        this.gridOffset = gridOffset;
    }

    public List<String> getTileNames() {
        return tileNames;
    }

    public void setTileNames(List<String> tileNames) {
        this.tileNames = tileNames;
    }
}

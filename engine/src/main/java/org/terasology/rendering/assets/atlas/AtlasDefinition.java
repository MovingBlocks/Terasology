// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.assets.atlas;

import org.joml.Vector2i;

import java.util.List;

/**
 */
public class AtlasDefinition {

    private String texture;
    private Vector2i textureSize;
    private GridDefinition grid;
    private List<GridDefinition> grids;
    private FreeformDefinition subimage;
    private List<FreeformDefinition> subimages;

    public String getTexture() {
        return texture;
    }

    public void setTexture(String texture) {
        this.texture = texture;
    }

    public Vector2i getTextureSize() {
        return textureSize;
    }

    public void setTextureSize(Vector2i textureSize) {
        this.textureSize = textureSize;
    }

    public GridDefinition getGrid() {
        return grid;
    }

    public void setGrid(GridDefinition grid) {
        this.grid = grid;
    }

    public List<GridDefinition> getGrids() {
        return grids;
    }

    public void setGrids(List<GridDefinition> grids) {
        this.grids = grids;
    }

    public FreeformDefinition getSubimage() {
        return subimage;
    }

    public void setSubimage(FreeformDefinition subimage) {
        this.subimage = subimage;
    }

    public List<FreeformDefinition> getSubimages() {
        return subimages;
    }

    public void setSubimages(List<FreeformDefinition> subimages) {
        this.subimages = subimages;
    }
}

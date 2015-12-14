/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.rendering.assets.atlas;

import org.terasology.math.geom.Vector2i;

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

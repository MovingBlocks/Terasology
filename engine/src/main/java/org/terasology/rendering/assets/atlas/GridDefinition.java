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

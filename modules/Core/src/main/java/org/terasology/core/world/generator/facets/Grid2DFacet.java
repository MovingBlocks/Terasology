/*
 * Copyright 2019 MovingBlocks
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
package org.terasology.core.world.generator.facets;

import org.terasology.math.Region3i;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2i;
import org.terasology.world.generation.Border3D;
import org.terasology.world.generation.facets.base.BaseFacet2D;


/**
 * This facet will allow to have only a small number of gridpoints embedded in a larger region.
 * It can be used as storage for world data after worldgeneration
 *
 */

public abstract class Grid2DFacet extends BaseFacet2D {

    protected int gridSize;
    protected Vector2i center;
    protected Rect2i gridWorldRegion;
    protected Rect2i gridRelativeRegion;

    public Grid2DFacet(Region3i targetRegion, Border3D border, int gridSize) {
        super(targetRegion, border);
        this.gridSize = gridSize;
        center = new Vector2i(targetRegion.center().x(), targetRegion.center().z());
        gridWorldRegion = Rect2i.createFromMinAndMax(center.x() - targetRegion.sizeX() / (2 * gridSize),
                center.y() - targetRegion.sizeY() / (2 * gridSize),
                center.x() + targetRegion.sizeX() / (2 * gridSize),
                center.y() + targetRegion.sizeY() / (2 * gridSize));

        gridRelativeRegion = Rect2i.createFromMinAndMax(0, 0,
                targetRegion.sizeX() / gridSize,
                targetRegion.sizeY() / gridSize);
    }

    public Vector2i getWorldPoint(Vector2i gridPoint) {
        return getWorldPoint(gridPoint.x(), gridPoint.y());
    }

    public Vector2i getWorldPoint(int x, int y) {
        if (!gridWorldRegion.contains(x,y)) {
            throw new IllegalArgumentException(String.format("Out of bounds: (%d, %d) for region %s", x, y, gridWorldRegion.toString()));
        }
        int xRelative = x - center.x();
        int yRelative = y - center.y();
        int xNew = center.x() + Math.round((float) xRelative * gridSize);
        int yNew = center.y() + Math.round((float) yRelative * gridSize);
        Vector2i gridPoint = new Vector2i(xNew, yNew);
        if (!getWorldRegion().contains(gridPoint)) {
            throw new IllegalArgumentException(String.format("Out of bounds: (%d, %d) for region %s", xNew, yNew, getWorldRegion().toString()));
        }
        return gridPoint;
    }

    public Vector2i getRelativeGridPoint(Vector2i worldPoint) {
        return getRelativeGridPoint(worldPoint.x(), worldPoint.y());
    }

    public Vector2i getRelativeGridPoint(int x, int y) {
        /*
        if (!getRelativeRegion().contains(x, y)) {
            throw new IllegalArgumentException(String.format("Out of bounds: (%d, %d) for region %s", x, y, getRelativeRegion().toString()));
        }*/
        int xNew = Math.round((float) x / gridSize);
        int yNew = Math.round((float) y / gridSize);
        Vector2i gridPoint = new Vector2i(xNew, yNew);
        if (!gridRelativeRegion.contains(gridPoint)) {
            throw new IllegalArgumentException(String.format("Out of bounds: (%d, %d) for region %s", xNew, yNew, gridRelativeRegion.toString()));
        }
        return gridPoint;
    }

    public Vector2i getWorldGridPoint(Vector2i worldPoint) {
        return getRelativeGridPoint(worldPoint).add(center);
    }

    public Vector2i getWorldGridPoint(int x, int y) {
        if (!getWorldRegion().contains(x, y)) {
            throw new IllegalArgumentException(String.format("Out of bounds: (%d, %d) for region %s", x, y, getWorldRegion().toString()));
        }
        int xRelative = x - center.x();
        int yRelative = y - center.y();
        int xNew = center.x() + Math.round((float) xRelative / gridSize);
        int yNew = center.y() + Math.round((float) yRelative / gridSize);
        Vector2i gridPoint = new Vector2i(xNew, yNew);
        if (!gridWorldRegion.contains(gridPoint)) {
            throw new IllegalArgumentException(String.format("Out of bounds: (%d, %d) for region %s", xNew, yNew, gridWorldRegion.toString()));
        }
        return gridPoint;
    }

    public int getGridSize() {
        return gridSize;
    }

    public Vector2i getCenter() {
        return center;
    }

    protected final int getRelativeGridIndex(int x, int z) {
        if (!gridRelativeRegion.contains(x, z)) {
            throw new IllegalArgumentException(String.format("Out of bounds: (%d, %d) for region %s", x, z, gridWorldRegion.toString()));
        }
        return x - gridRelativeRegion.minX() + gridRelativeRegion.sizeX() * (z - gridRelativeRegion.minY());
    }

    protected final int getWorldGridIndex(int x, int z) {
        if (!gridWorldRegion.contains(x, z)) {
            throw new IllegalArgumentException(String.format("Out of bounds: (%d, %d) for region %s", x, z, gridWorldRegion.toString()));
        }
        return x - gridWorldRegion.minX() + gridWorldRegion.sizeX() * (z - gridWorldRegion.minY());
    }

    public Rect2i getGridWorldRegion() {
        return gridWorldRegion;
    }

    public Rect2i getGridRelativeRegion() {
        return gridRelativeRegion;
    }
}

// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.generation.facets.base;

import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.joml.Vector3i;
import org.terasology.engine.world.block.BlockAreac;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.engine.world.generation.Border3D;
import org.terasology.engine.world.generation.WorldFacet2D;

/***
 * A base class for sparse (map-based) 2D facets.
 */
public abstract class BaseSparseFacet2D implements WorldFacet2D {
    private BlockAreac worldArea;
    private BlockAreac relativeArea;

    public BaseSparseFacet2D(BlockRegion targetRegion, Border3D border) {
        worldArea = border.expandTo2D(targetRegion);
        relativeArea = border.expandTo2D(targetRegion.getSize(new Vector3i()));
    }

    @Override
    public BlockAreac getWorldArea() {
        return worldArea;
    }

    @Override
    public BlockAreac getRelativeArea() {
        return relativeArea;
    }

    protected Vector2ic worldToRelative(int x, int y) {
        return new Vector2i(x - getWorldArea().minX() + getRelativeArea().minX(),
                y - getWorldArea().minY() + getRelativeArea().minY());
    }

    protected void validateCoord(int x, int y, BlockAreac area) {
        if(!area.contains(x, y)) {
            String text = "Out of bounds: (%d, %d) for region %s";
            String msg = String.format(text, x, y, area.toString());
            throw new IllegalArgumentException(msg);
        }
    }
}

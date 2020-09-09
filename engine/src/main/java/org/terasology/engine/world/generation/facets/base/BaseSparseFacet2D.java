// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.generation.facets.base;

import org.terasology.engine.math.Region3i;
import org.terasology.math.geom.BaseVector2i;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2i;
import org.terasology.engine.world.generation.Border3D;
import org.terasology.engine.world.generation.WorldFacet2D;

/***
 * A base class for sparse (map-based) 2D facets.
 */
public abstract class BaseSparseFacet2D implements WorldFacet2D {
    private final Rect2i worldRegion;
    private final Rect2i relativeRegion;

    public BaseSparseFacet2D(Region3i targetRegion, Border3D border) {
        worldRegion = border.expandTo2D(targetRegion);
        relativeRegion = border.expandTo2D(targetRegion.size());
    }

    @Override
    public Rect2i getWorldRegion() {
        return worldRegion;
    }

    @Override
    public Rect2i getRelativeRegion() {
        return relativeRegion;
    }

    protected BaseVector2i worldToRelative(int x, int y) {
        return new Vector2i(x - getWorldRegion().minX() + getRelativeRegion().minX(),
                y - getWorldRegion().minY() + getRelativeRegion().minY());
    }

    protected void validateCoord(int x, int y, Rect2i region) {
        if(!region.contains(x, y)) {
            String text = "Out of bounds: (%d, %d) for region %s";
            String msg = String.format(text, x, y, region.toString());
            throw new IllegalArgumentException(msg);
        }
    }
}

// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.generation.facets.base;

import org.terasology.engine.math.Region3i;
import org.terasology.math.geom.BaseVector2i;
import org.terasology.math.geom.Vector2i;
import org.terasology.engine.world.generation.Border3D;

import java.util.*;

/***
 * A strictly-sparse (not necessarily defined at all points) alternative to {@link BaseFieldFacet2D}
 */
public abstract class BaseStrictlySparseFieldFacet2D extends BaseSparseFacet2D {
    private final HashMap<BaseVector2i, Float> data = new HashMap<>();

    public BaseStrictlySparseFieldFacet2D(Region3i targetRegion, Border3D border) {
        super(targetRegion, border);
    }

    public Optional<Float> get(int x, int y) {
        return get(new Vector2i(x, y));
    }

    public Optional<Float> get(BaseVector2i pos) {
        validateCoord(pos.x(), pos.y(), getRelativeRegion());

        return Optional.ofNullable(data.getOrDefault(pos, null));
    }

    public Optional<Float> getWorld(int x, int y) {
        validateCoord(x, y, getWorldRegion());

        return Optional.ofNullable(data.getOrDefault(worldToRelative(x, y), null));
    }

    public Optional<Float> getWorld(BaseVector2i pos) {
        return getWorld(pos.x(), pos.y());
    }

    public void set(int x, int y, float value) {
        set(new Vector2i(x, y), value);
    }

    public void set(BaseVector2i pos, float value) {
        validateCoord(pos.x(), pos.y(), getRelativeRegion());

        data.put(pos, value);
    }

    public void setWorld(int x, int y, float value) {
        validateCoord(x, y, getWorldRegion());

        data.put(worldToRelative(x, y), value);
    }

    public void setWorld(BaseVector2i pos, float value) {
        setWorld(pos.x(), pos.y(), value);
    }

    public void unset(int x, int y) {
        unset(new Vector2i(x, y));
    }

    public void unset(BaseVector2i pos) {
        validateCoord(pos.x(), pos.y(), getRelativeRegion());

        data.remove(pos);
    }

    public void unsetWorld(int x, int y) {
        validateCoord(x, y, getWorldRegion());

        data.remove(worldToRelative(x, y));
    }

    public void unsetWorld(BaseVector2i pos) {
        unsetWorld(pos.x(), pos.y());
    }
}

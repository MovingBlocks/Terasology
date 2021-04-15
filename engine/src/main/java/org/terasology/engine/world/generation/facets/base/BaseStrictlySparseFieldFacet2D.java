// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.generation.facets.base;

import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.engine.world.generation.Border3D;

import java.util.HashMap;
import java.util.Optional;

/***
 * A strictly-sparse (not necessarily defined at all points) alternative to {@link BaseFieldFacet2D}
 */
public abstract class BaseStrictlySparseFieldFacet2D extends BaseSparseFacet2D {
    private HashMap<Vector2ic, Float> data = new HashMap<>();

    public BaseStrictlySparseFieldFacet2D(BlockRegion targetRegion, Border3D border) {
        super(targetRegion, border);
    }

    public Optional<Float> get(int x, int y) {
        return get(new Vector2i(x, y));
    }

    public Optional<Float> get(Vector2ic pos) {
        validateCoord(pos.x(), pos.y(), getRelativeArea());

        return Optional.ofNullable(data.getOrDefault(pos, null));
    }

    public Optional<Float> getWorld(int x, int y) {
        validateCoord(x, y, getWorldArea());

        return Optional.ofNullable(data.getOrDefault(worldToRelative(x, y), null));
    }

    public Optional<Float> getWorld(Vector2ic pos) {
        return getWorld(pos.x(), pos.y());
    }

    public void set(int x, int y, float value) {
        set(new Vector2i(x, y), value);
    }

    public void set(Vector2ic pos, float value) {
        validateCoord(pos.x(), pos.y(), getRelativeArea());

        data.put(pos, value);
    }

    public void setWorld(int x, int y, float value) {
        validateCoord(x, y, getWorldArea());

        data.put(worldToRelative(x, y), value);
    }

    public void setWorld(Vector2ic pos, float value) {
        setWorld(pos.x(), pos.y(), value);
    }

    public void unset(int x, int y) {
        unset(new Vector2i(x, y));
    }

    public void unset(Vector2ic pos) {
        validateCoord(pos.x(), pos.y(), getRelativeArea());

        data.remove(pos);
    }

    public void unsetWorld(int x, int y) {
        validateCoord(x, y, getWorldArea());

        data.remove(worldToRelative(x, y));
    }

    public void unsetWorld(Vector2ic pos) {
        unsetWorld(pos.x(), pos.y());
    }
}

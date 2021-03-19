// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.generation.facets.base;

import com.google.common.base.Preconditions;
import org.joml.Vector2ic;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.engine.world.generation.Border3D;

/**
 * An abstract, but complete implementation of {@link BooleanFieldFacet2D} that
 * is backed by a primitive boolean array.
 *
 */
public abstract class BaseBooleanFieldFacet2D extends BaseFacet2D implements BooleanFieldFacet2D {

    private final boolean[] data;

    public BaseBooleanFieldFacet2D(BlockRegion targetRegion, Border3D border) {
        super(targetRegion, border);
        data = new boolean[getRelativeArea().area()];
    }

    @Override
    public boolean get(int x, int y) {
        return data[getRelativeIndex(x, y)];
    }

    @Override
    public boolean get(Vector2ic pos) {
        return get(pos.x(), pos.y());
    }

    @Override
    public boolean getWorld(int x, int y) {
        return data[getWorldIndex(x, y)];
    }

    @Override
    public boolean getWorld(Vector2ic pos) {
        return getWorld(pos.x(), pos.y());
    }

    /**
     * This method exists for performance reasons, but it
     * is recommended to use proper getters and setters instead.
     * @return the internal data buffer
     */
    public boolean[] getInternal() {
        return data;
    }

    @Override
    public void set(int x, int y, boolean value) {
        data[getRelativeIndex(x, y)] = value;
    }

    @Override
    public void set(Vector2ic pos, boolean value) {
        set(pos.x(), pos.y(), value);
    }

    @Override
    public void setWorld(int x, int y, boolean value) {
        data[getWorldIndex(x, y)] = value;
    }

    @Override
    public void setWorld(Vector2ic pos, boolean value) {
        setWorld(pos.x(), pos.y(), value);
    }

    /**
     * Replaces the content of backing entirely with new data.
     * @param newData the new data (must be of size width * height)
     * @throws IllegalArgumentException if the size does not match
     */
    public void set(boolean[] newData) {
        Preconditions.checkArgument(newData.length == data.length, "Length must be %s, but is %s", data.length, newData.length);
        System.arraycopy(newData, 0, data, 0, newData.length);
    }

}

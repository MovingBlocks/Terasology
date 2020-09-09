// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.generation.facets.base;

import com.google.common.base.Preconditions;

import org.terasology.engine.math.Region3i;
import org.terasology.math.geom.BaseVector2i;
import org.terasology.math.geom.Vector2i;
import org.terasology.engine.world.generation.Border3D;

/**
 */
public abstract class BaseFieldFacet2D extends BaseFacet2D implements FieldFacet2D {

    private final float[] data;

    public BaseFieldFacet2D(Region3i targetRegion, Border3D border) {
        super(targetRegion, border);
        Vector2i size = getRelativeRegion().size();
        this.data = new float[size.x * size.y];
    }

    @Override
    public float get(int x, int y) {
        return data[getRelativeIndex(x, y)];
    }

    @Override
    public float get(BaseVector2i pos) {
        return get(pos.x(), pos.y());
    }

    @Override
    public float getWorld(int x, int y) {
        return data[getWorldIndex(x, y)];
    }

    @Override
    public float getWorld(BaseVector2i pos) {
        return getWorld(pos.x(), pos.y());
    }

    public float[] getInternal() {
        return data;
    }

    @Override
    public void set(int x, int y, float value) {
        data[getRelativeIndex(x, y)] = value;
    }

    @Override
    public void set(BaseVector2i pos, float value) {
        set(pos.x(), pos.y(), value);
    }

    @Override
    public void setWorld(int x, int y, float value) {
        data[getWorldIndex(x, y)] = value;
    }

    @Override
    public void setWorld(BaseVector2i pos, float value) {
        setWorld(pos.x(), pos.y(), value);
    }

    public void set(float[] newData) {
        Preconditions.checkArgument(newData.length == data.length, "New data must have same length as existing");
        System.arraycopy(newData, 0, data, 0, newData.length);
    }
}

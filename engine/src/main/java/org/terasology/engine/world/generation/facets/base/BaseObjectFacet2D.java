// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.generation.facets.base;

import com.google.common.base.Preconditions;

import org.terasology.engine.math.Region3i;
import org.terasology.math.geom.BaseVector2i;
import org.terasology.math.geom.Vector2i;
import org.terasology.engine.world.generation.Border3D;

import java.lang.reflect.Array;

/**
 */
public abstract class BaseObjectFacet2D<T> extends BaseFacet2D implements ObjectFacet2D<T> {

    private final T[] data;

    public BaseObjectFacet2D(Region3i targetRegion, Border3D border, Class<T> objectType) {
        super(targetRegion, border);
        Vector2i size = getRelativeRegion().size();
        this.data = (T[]) Array.newInstance(objectType, size.x * size.y);
    }

    @Override
    public T get(int x, int y) {
        return data[getRelativeIndex(x, y)];
    }

    @Override
    public T get(BaseVector2i pos) {
        return get(pos.x(), pos.y());
    }

    @Override
    public T getWorld(int x, int y) {
        return data[getWorldIndex(x, y)];
    }

    @Override
    public T getWorld(BaseVector2i pos) {
        return getWorld(pos.x(), pos.y());
    }

    public T[] getInternal() {
        return data;
    }

    @Override
    public void set(int x, int y, T value) {
        data[getRelativeIndex(x, y)] = value;
    }

    @Override
    public void set(BaseVector2i pos, T value) {
        set(pos.getX(), pos.getY(), value);
    }

    @Override
    public void setWorld(int x, int y, T value) {
        data[getWorldIndex(x, y)] = value;
    }

    @Override
    public void setWorld(BaseVector2i pos, T value) {
        setWorld(pos.x(), pos.y(), value);
    }

    public void set(T[] newData) {
        Preconditions.checkArgument(newData.length == data.length);
        System.arraycopy(newData, 0, data, 0, newData.length);
    }
}

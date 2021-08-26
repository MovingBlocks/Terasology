// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.generation.facets.base;

import com.google.common.base.Preconditions;
import org.joml.Vector2ic;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.engine.world.generation.Border3D;

import java.lang.reflect.Array;

public abstract class BaseObjectFacet2D<T> extends BaseFacet2D implements ObjectFacet2D<T> {

    private T[] data;

    public BaseObjectFacet2D(BlockRegion targetRegion, Border3D border, Class<T> objectType) {
        super(targetRegion, border);
        this.data = (T[]) Array.newInstance(objectType, getRelativeArea().area());
    }

    @Override
    public T get(int x, int y) {
        return data[getRelativeIndex(x, y)];
    }

    @Override
    public T get(Vector2ic pos) {
        return get(pos.x(), pos.y());
    }

    @Override
    public T getWorld(int x, int y) {
        return data[getWorldIndex(x, y)];
    }

    @Override
    public T getWorld(Vector2ic pos) {
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
    public void set(Vector2ic pos, T value) {
        set(pos.x(), pos.y(), value);
    }

    @Override
    public void setWorld(int x, int y, T value) {
        data[getWorldIndex(x, y)] = value;
    }

    @Override
    public void setWorld(Vector2ic pos, T value) {
        setWorld(pos.x(), pos.y(), value);
    }

    public void set(T[] newData) {
        Preconditions.checkArgument(newData.length == data.length);
        System.arraycopy(newData, 0, data, 0, newData.length);
    }
}

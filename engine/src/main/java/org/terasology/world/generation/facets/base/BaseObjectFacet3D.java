// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.generation.facets.base;

import com.google.common.base.Preconditions;
import org.joml.Vector3ic;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.engine.world.generation.Border3D;

import java.lang.reflect.Array;

/**
 * Base class for storing objects of the specified type in a 3D grid for a facet.
 *
 * @param <T> Type of objects stored.
 */
public abstract class BaseObjectFacet3D<T> extends BaseFacet3D implements ObjectFacet3D<T> {
    private T[] data;

    public BaseObjectFacet3D(BlockRegion targetRegion, Border3D border, Class<T> objectType) {
        super(targetRegion, border);
        this.data = (T[]) Array.newInstance(objectType, getRelativeRegion().volume());
    }

    @Override
    public T get(int x, int y, int z) {
        return data[getRelativeIndex(x, y, z)];
    }

    @Override
    public T get(Vector3ic pos) {
        return get(pos.x(), pos.y(), pos.z());
    }

    @Override
    public T getWorld(int x, int y, int z) {
        return data[getWorldIndex(x, y, z)];
    }

    @Override
    public T getWorld(Vector3ic pos) {
        return getWorld(pos.x(), pos.y(), pos.z());
    }

    public T[] getInternal() {
        return data;
    }

    @Override
    public void set(int x, int y, int z, T value) {
        data[getRelativeIndex(x, y, z)] = value;
    }

    @Override
    public void set(Vector3ic pos, T value) {
        set(pos.x(), pos.y(), pos.z(), value);
    }

    @Override
    public void setWorld(int x, int y, int z, T value) {
        data[getWorldIndex(x, y, z)] = value;
    }

    @Override
    public void setWorld(Vector3ic pos, T value) {
        setWorld(pos.x(), pos.y(), pos.z(), value);
    }

    public void set(T[] newData) {
        Preconditions.checkArgument(newData.length == data.length);
        System.arraycopy(newData, 0, data, 0, newData.length);
    }
}

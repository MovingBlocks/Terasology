// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.generation.facets.base;

import java.lang.reflect.Array;

import org.terasology.engine.math.Region3i;
import org.terasology.math.geom.BaseVector3i;
import org.terasology.math.geom.Vector3i;
import org.terasology.engine.world.generation.Border3D;

import com.google.common.base.Preconditions;

/**
 * Base class for storing objects of the specified type in a 3D grid for a facet.
 *
 * @param <T> Type of objects stored.
 */
public abstract class BaseObjectFacet3D<T> extends BaseFacet3D implements ObjectFacet3D<T> {
    private final T[] data;

    public BaseObjectFacet3D(Region3i targetRegion, Border3D border, Class<T> objectType) {
        super(targetRegion, border);
        Vector3i size = getRelativeRegion().size();
        this.data = (T[]) Array.newInstance(objectType, size.x * size.y * size.z);
    }

    @Override
    public T get(int x, int y, int z) {
        return data[getRelativeIndex(x, y, z)];
    }

    @Override
    public T get(BaseVector3i pos) {
        return get(pos.x(), pos.y(), pos.z());
    }

    @Override
    public T getWorld(int x, int y, int z) {
        return data[getWorldIndex(x, y, z)];
    }

    @Override
    public T getWorld(BaseVector3i pos) {
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
    public void set(BaseVector3i pos, T value) {
        set(pos.x(), pos.y(), pos.z(), value);
    }

    @Override
    public void setWorld(int x, int y, int z, T value) {
        data[getWorldIndex(x, y, z)] = value;
    }

    @Override
    public void setWorld(BaseVector3i pos, T value) {
        setWorld(pos.x(), pos.y(), pos.z(), value);
    }

    public void set(T[] newData) {
        Preconditions.checkArgument(newData.length == data.length);
        System.arraycopy(newData, 0, data, 0, newData.length);
    }
}

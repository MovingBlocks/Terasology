/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.world.generation.facets.base;

import java.lang.reflect.Array;

import org.terasology.math.Region3i;
import org.terasology.math.geom.BaseVector3i;
import org.terasology.math.geom.Vector3i;
import org.terasology.world.generation.Border3D;

import com.google.common.base.Preconditions;

/**
 * Base class for storing objects of the specified type in a 3D grid for a facet.
 *
 * @param <T> Type of objects stored.
 */
public abstract class BaseObjectFacet3D<T> extends BaseFacet3D implements ObjectFacet3D<T> {
    private T[] data;

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

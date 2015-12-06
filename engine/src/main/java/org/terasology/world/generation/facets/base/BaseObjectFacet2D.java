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

import com.google.common.base.Preconditions;

import org.terasology.math.Region3i;
import org.terasology.math.geom.BaseVector2i;
import org.terasology.math.geom.Vector2i;
import org.terasology.world.generation.Border3D;

import java.lang.reflect.Array;

/**
 */
public abstract class BaseObjectFacet2D<T> extends BaseFacet2D implements ObjectFacet2D<T> {

    private T[] data;

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

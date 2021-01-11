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
import org.joml.Vector2ic;
import org.terasology.world.block.BlockRegion;
import org.terasology.world.generation.Border3D;

import java.lang.reflect.Array;

/**
 */
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

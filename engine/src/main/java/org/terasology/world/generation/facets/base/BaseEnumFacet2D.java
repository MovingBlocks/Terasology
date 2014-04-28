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
import org.terasology.math.Vector2i;

import java.lang.reflect.Array;

/**
 * @author Immortius
 */
public class BaseEnumFacet2D<T extends Enum> implements EnumFacet2D<T> {

    private Vector2i size = new Vector2i();
    private T[] data;

    public BaseEnumFacet2D(Vector2i size, Class<T> enumType) {
        this.size.set(size);
        this.data = (T[]) Array.newInstance(enumType, size.getX() * size.getY());
    }

    @Override
    public T get(int x, int y) {
        return data[x + size.getX() * y];
    }

    @Override
    public T get(Vector2i pos) {
        return get(pos.x, pos.y);
    }

    @Override
    public T[] getInternal() {
        return data;
    }

    @Override
    public void set(int x, int y, T value) {
        data[x + size.getX() * y] = value;
    }

    @Override
    public void set(Vector2i pos, T value) {
        set(pos.getX(), pos.getY(), value);
    }

    @Override
    public void set(int index, T value) {
        data[index] = value;
    }

    @Override
    public void set(T[] newData) {
        Preconditions.checkArgument(newData.length == data.length);
        System.arraycopy(newData, 0, data, 0, newData.length);
    }
}

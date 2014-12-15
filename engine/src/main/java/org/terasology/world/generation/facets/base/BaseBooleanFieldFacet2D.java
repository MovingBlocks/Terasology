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
import org.terasology.math.Vector2i;
import org.terasology.world.generation.Border3D;

/**
 * @author Immortius
 */
public abstract class BaseBooleanFieldFacet2D extends BaseFacet2D implements BooleanFieldFacet2D {

    private boolean[] data;

    public BaseBooleanFieldFacet2D(Region3i targetRegion, Border3D border) {
        super(targetRegion, border);
        Vector2i size = getRelativeRegion().size();
        data = new boolean[size.x * size.y];
    }

    @Override
    public boolean get(int x, int y) {
        return data[getRelativeIndex(x, y)];
    }

    @Override
    public boolean get(Vector2i pos) {
        return get(pos.x, pos.y);
    }

    @Override
    public boolean getWorld(int x, int y) {
        return data[getWorldIndex(x, y)];
    }

    @Override
    public boolean getWorld(Vector2i pos) {
        return getWorld(pos.x, pos.y);
    }

    public boolean[] getInternal() {
        return data;
    }

    @Override
    public void set(int x, int y, boolean value) {
        data[getRelativeIndex(x, y)] = value;
    }

    @Override
    public void set(Vector2i pos, boolean value) {
        set(pos.x, pos.y, value);
    }

    @Override
    public void setWorld(int x, int y, boolean value) {
        data[getWorldIndex(x, y)] = value;
    }

    @Override
    public void setWorld(Vector2i pos, boolean value) {
        setWorld(pos.x, pos.y, value);
    }

    public void set(boolean[] newData) {
        Preconditions.checkArgument(newData.length == data.length);
        System.arraycopy(newData, 0, data, 0, newData.length);
    }

}

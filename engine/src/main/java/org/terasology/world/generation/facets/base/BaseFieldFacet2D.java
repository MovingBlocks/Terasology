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

/**
 */
public abstract class BaseFieldFacet2D extends BaseFacet2D implements FieldFacet2D {

    private float[] data;

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

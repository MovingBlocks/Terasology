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
import org.terasology.math.geom.Vector3i;
import org.terasology.world.generation.Border3D;

/**
 */
public abstract class BaseFieldFacet3D extends BaseFacet3D implements FieldFacet3D {

    private float[] data;

    public BaseFieldFacet3D(Region3i targetRegion, Border3D border) {
        super(targetRegion, border);
        Vector3i size = getRelativeRegion().size();
        this.data = new float[size.x * size.y * size.z];
    }

    @Override
    public float get(int x, int y, int z) {
        return data[getRelativeIndex(x, y, z)];
    }

    @Override
    public float get(Vector3i pos) {
        return get(pos.x, pos.y, pos.z);
    }

    @Override
    public float getWorld(int x, int y, int z) {
        return data[getWorldIndex(x, y, z)];
    }

    @Override
    public float getWorld(Vector3i pos) {
        return getWorld(pos.x, pos.y, pos.z);
    }

    public float[] getInternal() {
        return data;
    }

    @Override
    public void set(int x, int y, int z, float value) {
        data[getRelativeIndex(x, y, z)] = value;
    }

    @Override
    public void set(Vector3i pos, float value) {
        set(pos.x, pos.y, pos.z, value);
    }

    @Override
    public void setWorld(int x, int y, int z, float value) {
        data[getWorldIndex(x, y, z)] = value;
    }

    @Override
    public void setWorld(Vector3i pos, float value) {
        setWorld(pos.x, pos.y, pos.z, value);
    }

    public void set(float[] newData) {
        Preconditions.checkArgument(newData.length == data.length);
        System.arraycopy(newData, 0, data, 0, newData.length);
    }
}

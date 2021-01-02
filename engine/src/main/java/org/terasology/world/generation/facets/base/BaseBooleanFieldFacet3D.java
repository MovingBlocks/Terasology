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
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.terasology.world.block.BlockRegion;
import org.terasology.world.generation.Border3D;

/**
 */
public abstract class BaseBooleanFieldFacet3D extends BaseFacet3D implements BooleanFieldFacet3D {

    private boolean[] data;

    public BaseBooleanFieldFacet3D(BlockRegion targetRegion, Border3D border) {
        super(targetRegion, border);
        data =
            new boolean[getRelativeRegion().volume()];
    }

    @Override
    public boolean get(int x, int y, int z) {
        return data[getRelativeIndex(x, y, z)];
    }

    @Override
    public boolean get(Vector3ic pos) {
        return get(pos.x(), pos.y(), pos.z());
    }

    @Override
    public boolean getWorld(int x, int y, int z) {
        return data[getWorldIndex(x, y, z)];
    }

    @Override
    public boolean getWorld(Vector3ic pos) {
        return getWorld(pos.x(), pos.y(), pos.z());
    }

    public boolean[] getInternal() {
        return data;
    }

    @Override
    public void set(int x, int y, int z, boolean value) {
        data[getRelativeIndex(x, y, z)] = value;
    }

    @Override
    public void set(Vector3ic pos, boolean value) {
        set(pos.x(), pos.y(), pos.z(), value);
    }

    @Override
    public void setWorld(int x, int y, int z, boolean value) {
        data[getWorldIndex(x, y, z)] = value;
    }

    @Override
    public void setWorld(Vector3ic pos, boolean value) {
        setWorld(pos.x(), pos.y(), pos.z(), value);
    }

    public void set(boolean[] newData) {
        Preconditions.checkArgument(newData.length == data.length);
        System.arraycopy(newData, 0, data, 0, newData.length);
    }
}

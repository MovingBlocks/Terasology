// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.generation.facets.base;

import com.google.common.base.Preconditions;
import org.terasology.engine.math.Region3i;
import org.terasology.math.geom.Vector3i;
import org.terasology.engine.world.generation.Border3D;

/**
 */
public abstract class BaseFieldFacet3D extends BaseFacet3D implements FieldFacet3D {

    private final float[] data;

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

// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.generation.facets.base;

import com.google.common.collect.Maps;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.terasology.engine.world.block.BlockRegionc;
import org.terasology.engine.world.generation.Border3D;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A sparse (map-based) implementation
 * of {@link ObjectFacet3D}.
 *
 */
public abstract class SparseObjectFacet3D<T> extends SparseFacet3D implements ObjectFacet3D<T> {

    private final Map<Vector3ic, T> relData = Maps.newLinkedHashMap();

    /**
     * @param targetRegion
     * @param border
     */
    public SparseObjectFacet3D(BlockRegionc targetRegion, Border3D border) {
        super(targetRegion, border);
    }

    @Override
    public T get(int x, int y, int z) {
        return get(new Vector3i(x, y, z));
    }

    @Override
    public T get(Vector3ic pos) {
        checkRelativeCoords(pos.x(), pos.y(), pos.z());

        return relData.get(pos);
    }

    @Override
    public void set(int x, int y, int z, T value) {
        set(new Vector3i(x, y, z), value);
    }

    @Override
    public void set(Vector3ic pos, T value) {
        checkRelativeCoords(pos.x(), pos.y(), pos.z());

        relData.put(pos, value); // TODO: consider using an immutable vector here
    }

    @Override
    public T getWorld(Vector3ic pos) {
        return getWorld(pos.x(), pos.y(), pos.z());
    }

    @Override
    public T getWorld(int x, int y, int z) {
        checkWorldCoords(x, y, z);

        Vector3i index = worldToRelative(x, y, z);
        return relData.get(index);
    }

    @Override
    public void setWorld(Vector3ic pos, T value) {
        setWorld(pos.x(), pos.y(), pos.z(), value);
    }

    @Override
    public void setWorld(int x, int y, int z, T value) {
        checkWorldCoords(x, y, z);

        Vector3i index = worldToRelative(x, y, z);
        relData.put(index, value);
    }

    /**
     * @return an unmodifiable view on the relative entries
     */
    public Map<Vector3ic, T> getRelativeEntries() {
        return Collections.unmodifiableMap(relData);
    }

    /**
     * @return a <b>new</b> map with world-based position entries
     */
    public Map<Vector3ic, T> getWorldEntries() {

        Map<Vector3ic, T> result = Maps.newLinkedHashMap();

        for (Entry<Vector3ic, T> entry : relData.entrySet()) {
            Vector3ic relPos = entry.getKey();
            Vector3ic worldPos = relativeToWorld(relPos.x(), relPos.y(), relPos.z());

            result.put(worldPos, entry.getValue());
        }

        return result;
    }
}

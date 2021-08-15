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
public abstract class SparseFieldFacet3D extends SparseFacet3D implements FieldFacet3D {

    // msteiger: it could be advantageous to use a Joda Collections map instead
    private final Map<Vector3ic, Number> relData = Maps.newLinkedHashMap();

    private final float defValue;

    public SparseFieldFacet3D(BlockRegionc targetRegion, Border3D border) {
        this(targetRegion, border, 0);
    }

    public SparseFieldFacet3D(BlockRegionc targetRegion, Border3D border, float defValue) {
        super(targetRegion, border);
        this.defValue = defValue;
    }

    @Override
    public float get(int x, int y, int z) {
        return get(new Vector3i(x, y, z));
    }

    @Override
    public float get(Vector3ic pos) {
        checkRelativeCoords(pos.x(), pos.y(), pos.z());

        Number n = relData.get(pos);
        return (n != null) ? n.floatValue() : defValue;
    }

    @Override
    public void set(int x, int y, int z, float value) {
        set(new Vector3i(x, y, z), value);
    }

    @Override
    public void set(Vector3ic pos, float value) {
        set(pos, Float.valueOf(value));
    }

    public void set(int x, int y, int z, Number value) {
        set(new Vector3i(x, y, z), value);
    }

    public void set(Vector3ic pos, Number value) {
        checkRelativeCoords(pos.x(), pos.y(), pos.z());

        if (value.floatValue() != defValue) {
            relData.put(pos, value);
        }
    }

    @Override
    public float getWorld(Vector3ic pos) {
        return getWorld(pos.x(), pos.y(), pos.z());
    }

    @Override
    public float getWorld(int x, int y, int z) {
        checkWorldCoords(x, y, z);

        Vector3i rel = worldToRelative(x, y, z);
        Number n = relData.get(rel);
        return (n != null) ? n.floatValue() : defValue;
    }

    @Override
    public void setWorld(Vector3ic pos, float value) {
        setWorld(pos.x(), pos.y(), pos.z(), value);
    }

    @Override
    public void setWorld(int x, int y, int z, float value) {
        setWorld(x, y, z, Float.valueOf(value));
    }

    public void setWorld(Vector3ic pos, Number value) {
        setWorld(pos.x(), pos.y(), pos.z(), value);
    }

    public void setWorld(int x, int y, int z, Number value) {
        checkWorldCoords(x, y, z);

        Vector3i rel = worldToRelative(x, y, z);
        if (value.floatValue() != defValue) {
            relData.put(rel, value);
        }
    }

    /**
     * @return an unmodifiable view on the relative entries
     */
    public Map<Vector3ic, Number> getRelativeEntries() {
        return Collections.unmodifiableMap(relData);
    }

    /**
     * @return a <b>new</b> map with world-based position entries
     */
    public Map<Vector3i, Number> getWorldEntries() {

        Map<Vector3i, Number> result = Maps.newLinkedHashMap();

        for (Entry<Vector3ic, Number> entry : relData.entrySet()) {
            Vector3ic relPos = entry.getKey();
            Vector3i worldPos = relativeToWorld(relPos.x(), relPos.y(), relPos.z());

            result.put(worldPos, entry.getValue());
        }

        return result;
    }
}

/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.world.generation.facets.base;

import com.google.common.collect.Maps;
import org.terasology.math.Region3i;
import org.terasology.math.geom.Vector3i;
import org.terasology.world.generation.Border3D;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A sparse (map-based) implementation
 * of {@link ObjectFacet3D}.
 *
 */
public abstract class SparseBooleanFieldFacet3D extends SparseFacet3D implements BooleanFieldFacet3D {

    private final Map<Vector3i, Boolean> relData = Maps.newLinkedHashMap();

    private final boolean defValue;

    public SparseBooleanFieldFacet3D(Region3i targetRegion, Border3D border) {
        this(targetRegion, border, false);
    }

    public SparseBooleanFieldFacet3D(Region3i targetRegion, Border3D border, boolean defValue) {
        super(targetRegion, border);
        this.defValue = defValue;
    }

    @Override
    public boolean get(int x, int y, int z) {
        return get(new Vector3i(x, y, z));
    }

    @Override
    public boolean get(Vector3i pos) {
        checkRelativeCoords(pos.x, pos.y, pos.z);

        Boolean boxed = relData.get(pos);
        return (boxed != null) ? boxed : defValue;
    }

    @Override
    public void set(int x, int y, int z, boolean value) {
        set(new Vector3i(x, y, z), value);
    }

    @Override
    public void set(Vector3i pos, boolean value) {
        checkRelativeCoords(pos.x, pos.y, pos.z);

        if (value != defValue) {
            relData.put(pos, value);
        }
    }

    @Override
    public boolean getWorld(Vector3i pos) {
        return getWorld(pos.x, pos.y, pos.z);
    }

    @Override
    public boolean getWorld(int x, int y, int z) {
        checkWorldCoords(x, y, z);

        Vector3i rel = worldToRelative(x, y, z);
        Boolean boxed = relData.get(rel);
        return (boxed != null) ? boxed : defValue;
    }

    @Override
    public void setWorld(Vector3i pos, boolean value) {
        setWorld(pos.x, pos.y, pos.z, value);
    }

    @Override
    public void setWorld(int x, int y, int z, boolean value) {
        checkWorldCoords(x, y, z);

        Vector3i rel = worldToRelative(x, y, z);
        if (value != defValue) {
            relData.put(rel, value);
        }
    }

    /**
     * @return an unmodifiable view on the relative entries
     */
    public Map<Vector3i, Boolean> getRelativeEntries() {
        return Collections.unmodifiableMap(relData);
    }

    /**
     * @return a <b>new</b> map with world-based position entries
     */
    public Map<Vector3i, Boolean> getWorldEntries() {

        Map<Vector3i, Boolean> result = Maps.newLinkedHashMap();

        for (Entry<Vector3i, Boolean> entry : relData.entrySet()) {
            Vector3i relPos = entry.getKey();
            Vector3i worldPos = relativeToWorld(relPos.x, relPos.y, relPos.z);

            result.put(worldPos, entry.getValue());
        }

        return result;
    }
}

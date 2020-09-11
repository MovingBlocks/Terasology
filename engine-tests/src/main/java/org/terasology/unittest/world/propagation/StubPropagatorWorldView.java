// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.unittest.world.propagation;

import com.google.common.collect.Maps;
import gnu.trove.map.TObjectByteMap;
import gnu.trove.map.hash.TObjectByteHashMap;
import org.terasology.engine.math.Region3i;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.propagation.PropagatorWorldView;
import org.terasology.math.geom.Vector3i;

import java.util.Map;

/**
 *
 */
public class StubPropagatorWorldView implements PropagatorWorldView {
    private final TObjectByteMap<Vector3i> lightData = new TObjectByteHashMap<>();
    private Map<Vector3i, Block> blockData = Maps.newHashMap();
    private final Region3i relevantRegion;
    private final Block defaultBlock;

    public StubPropagatorWorldView(Region3i relevantRegion, Block defaultBlock) {
        this.relevantRegion = relevantRegion;
        this.defaultBlock = defaultBlock;
    }

    public StubPropagatorWorldView(Region3i relevantRegion, Block defaultBlock, Map<Vector3i, Block> blockData) {
        this(relevantRegion, defaultBlock);
        this.blockData = blockData;
    }

    @Override
    public byte getValueAt(Vector3i pos) {
        if (!relevantRegion.encompasses(pos)) {
            return UNAVAILABLE;
        }
        return lightData.get(pos);
    }

    @Override
    public void setValueAt(Vector3i pos, byte value) {
        if (!relevantRegion.encompasses(pos)) {
            throw new IllegalArgumentException("Position out of bounds: " + pos);
        }
        lightData.put(new Vector3i(pos), value);
    }

    @Override
    public Block getBlockAt(Vector3i pos) {
        if (!relevantRegion.encompasses(pos)) {
            throw new IllegalArgumentException("Position out of bounds: " + pos);
        }

        Block result = blockData.get(pos);
        if (result == null) {
            return defaultBlock;
        }
        return result;
    }

    public void setBlockAt(Vector3i pos, Block block) {
        if (!relevantRegion.encompasses(pos)) {
            throw new IllegalArgumentException("Position out of bounds: " + pos);
        }

        blockData.put(pos, block);
    }
}

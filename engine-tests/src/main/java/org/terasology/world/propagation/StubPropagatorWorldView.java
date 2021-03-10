/*
 * Copyright 2013 MovingBlocks
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
package org.terasology.world.propagation;

import com.google.common.collect.Maps;
import gnu.trove.map.TObjectByteMap;
import gnu.trove.map.hash.TObjectByteHashMap;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.engine.world.block.BlockRegionc;
import org.terasology.engine.world.propagation.PropagatorWorldView;

import java.util.Map;

/**
 */
public class StubPropagatorWorldView implements PropagatorWorldView {
    private TObjectByteMap<Vector3ic> lightData = new TObjectByteHashMap<>();
    private Map<Vector3ic, Block> blockData = Maps.newHashMap();
    private BlockRegionc relevantRegion;
    private Block defaultBlock;

    public StubPropagatorWorldView(BlockRegionc relevantRegion, Block defaultBlock) {
        this.relevantRegion = new BlockRegion(relevantRegion);
        this.defaultBlock = defaultBlock;
    }

    public StubPropagatorWorldView(BlockRegionc relevantRegion, Block defaultBlock, Map<Vector3ic, Block> blockData) {
        this(relevantRegion, defaultBlock);
        this.blockData = blockData;
    }


    @Override
    public byte getValueAt(Vector3ic pos) {
        if (!relevantRegion.contains(pos)) {
            return UNAVAILABLE;
        }
        return lightData.get(pos);
    }

    @Override
    public void setValueAt(Vector3ic pos, byte value) {
        if (!relevantRegion.contains(pos)) {
            throw new IllegalArgumentException("Position out of bounds: " + pos);
        }
        lightData.put(new Vector3i(pos), value);
    }

    @Override
    public Block getBlockAt(Vector3ic pos) {
        if (!relevantRegion.contains(pos)) {
            throw new IllegalArgumentException("Position out of bounds: " + pos);
        }

        Block result = blockData.get(pos);
        if (result == null) {
            return defaultBlock;
        }
        return result;
    }

    public void setBlockAt(Vector3ic pos, Block block) {
        if (!relevantRegion.contains(pos)) {
            throw new IllegalArgumentException("Position out of bounds: " + pos);
        }

        blockData.put(pos, block);
    }
}

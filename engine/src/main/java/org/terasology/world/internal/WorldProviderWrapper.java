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

package org.terasology.world.internal;

import org.joml.Vector3fc;
import org.joml.Vector3ic;
import org.terasology.math.Region3i;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.world.WorldChangeListener;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockRegion;
import org.terasology.world.chunks.blockdata.ExtraBlockDataManager;

import java.math.RoundingMode;
import java.util.Collection;

/**
 */
public class WorldProviderWrapper extends AbstractWorldProviderDecorator implements WorldProvider {
    private WorldProviderCore core;
    private ExtraBlockDataManager extraDataManager;

    public WorldProviderWrapper(WorldProviderCore core, ExtraBlockDataManager extraDataManager) {
        super(core);
        this.core = core;
        this.extraDataManager = extraDataManager;
    }

    @Override
    public boolean isBlockRelevant(Vector3i pos) {
        return core.isBlockRelevant(pos.x, pos.y, pos.z);
    }

    @Override
    public boolean isBlockRelevant(Vector3ic pos) {
        return core.isBlockRelevant(pos.x(), pos.y(), pos.z());
    }

    @Override
    public boolean isBlockRelevant(Vector3f pos) {
        return isBlockRelevant(new Vector3i(pos, RoundingMode.HALF_UP));
    }

    @Override
    public boolean isBlockRelevant(Vector3fc pos) {
        return isBlockRelevant(new org.joml.Vector3i(pos, org.joml.RoundingMode.HALF_UP));
    }

    @Override
    public Block setBlock(Vector3i pos, Block type) {
        return core.setBlock(pos, type);
    }

    @Override
    public Block setBlock(Vector3ic pos, Block type) {
        return core.setBlock(pos, type);
    }

    @Override
    public Block getBlock(Vector3f pos) {
        return getBlock(new Vector3i(pos, RoundingMode.HALF_UP));
    }

    @Override
    public Block getBlock(Vector3fc pos) {
        return getBlock(new org.joml.Vector3i(pos, org.joml.RoundingMode.HALF_UP));
    }

    @Override
    public Block getBlock(Vector3i pos) {
        return core.getBlock(pos.x, pos.y, pos.z);
    }

    @Override
    public Block getBlock(Vector3ic pos) {
        return core.getBlock(pos.x(), pos.y(), pos.z());
    }

    @Override
    public byte getLight(Vector3i pos) {
        return core.getLight(pos.x, pos.y, pos.z);
    }

    @Override
    public byte getLight(Vector3f pos) {
        return getLight(new Vector3i(pos, RoundingMode.HALF_UP));
    }

    @Override
    public byte getSunlight(Vector3f pos) {
        return getSunlight(new Vector3i(pos, RoundingMode.HALF_UP));
    }

    @Override
    public byte getTotalLight(Vector3f pos) {
        return getTotalLight(new Vector3i(pos, RoundingMode.HALF_UP));
    }


    @Override
    public byte getSunlight(Vector3i pos) {
        return core.getSunlight(pos.x, pos.y, pos.z);
    }

    @Override
    public byte getTotalLight(Vector3i pos) {
        return core.getTotalLight(pos.x, pos.y, pos.z);
    }

    public int getExtraData(int index, Vector3i pos) {
        return core.getExtraData(index, pos.x, pos.y, pos.z);
    }

    public int setExtraData(int index, int x, int y, int z, int value) {
        return core.setExtraData(index, new Vector3i(x, y, z), value);
    }

    public int getExtraData(String fieldName, int x, int y, int z) {
        return core.getExtraData(extraDataManager.getSlotNumber(fieldName), x, y, z);
    }

    public int getExtraData(String fieldName, Vector3i pos) {
        return core.getExtraData(extraDataManager.getSlotNumber(fieldName), pos.x, pos.y, pos.z);
    }

    public int setExtraData(String fieldName, int x, int y, int z, int value) {
        return core.setExtraData(extraDataManager.getSlotNumber(fieldName), new Vector3i(x, y, z), value);
    }

    public int setExtraData(String fieldName, Vector3i pos, int value) {
        return core.setExtraData(extraDataManager.getSlotNumber(fieldName), pos, value);
    }

    @Override
    public void processPropagation() {
        core.processPropagation();
    }

    @Override
    public void registerListener(WorldChangeListener listener) {
        core.registerListener(listener);
    }

    @Override
    public void unregisterListener(WorldChangeListener listener) {
        core.unregisterListener(listener);
    }

    @Override
    public Collection<BlockRegion> getRelevantRegions() {
        return core.getRelevantRegions();
    }
}

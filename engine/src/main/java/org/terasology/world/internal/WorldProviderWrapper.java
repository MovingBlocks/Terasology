// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.world.internal;

import org.joml.RoundingMode;
import org.joml.Vector3fc;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.terasology.world.WorldChangeListener;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockRegionc;
import org.terasology.world.chunks.blockdata.ExtraBlockDataManager;

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
    public boolean isBlockRelevant(Vector3ic pos) {
        return core.isBlockRelevant(pos.x(), pos.y(), pos.z());
    }

    @Override
    public boolean isBlockRelevant(Vector3fc pos) {
        return isBlockRelevant(new Vector3i(pos, RoundingMode.HALF_UP));
    }

    @Override
    public Block setBlock(Vector3ic pos, Block type) {
        return core.setBlock(pos, type);
    }

    @Override
    public Block getBlock(Vector3fc pos) {
        return getBlock(new Vector3i(pos, RoundingMode.HALF_UP));
    }

    @Override
    public byte getLight(Vector3ic pos) {
        return core.getLight(pos.x(), pos.y(), pos.z());
    }

    @Override
    public byte getLight(Vector3fc pos) {
        return getLight(new Vector3i(pos, RoundingMode.FLOOR));
    }

    @Override
    public byte getSunlight(Vector3fc pos) {
        return getSunlight(new Vector3i(pos, RoundingMode.HALF_UP));
    }

    @Override
    public byte getTotalLight(Vector3fc pos) {
        return getTotalLight(new Vector3i(pos, RoundingMode.HALF_UP));
    }

    @Override
    public byte getSunlight(Vector3ic pos) {
        return core.getSunlight(pos.x(), pos.y(), pos.z());
    }

    @Override
    public byte getTotalLight(Vector3ic pos) {
        return core.getTotalLight(pos.x(), pos.y(), pos.z());
    }

    public int setExtraData(int index, int x, int y, int z, int value) {
        return core.setExtraData(index, new Vector3i(x, y, z), value);
    }

    public int getExtraData(String fieldName, int x, int y, int z) {
        return core.getExtraData(extraDataManager.getSlotNumber(fieldName), x, y, z);
    }

    public int setExtraData(String fieldName, int x, int y, int z, int value) {
        return core.setExtraData(extraDataManager.getSlotNumber(fieldName), new Vector3i(x, y, z), value);
    }

    public int setExtraData(String fieldName, Vector3ic pos, int value) {
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
    public Collection<BlockRegionc> getRelevantRegions() {
        return core.getRelevantRegions();
    }
}

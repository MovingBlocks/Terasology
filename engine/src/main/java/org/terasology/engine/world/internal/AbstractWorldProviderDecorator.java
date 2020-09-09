// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.internal;

import org.joml.Vector3ic;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.math.Region3i;
import org.terasology.engine.world.WorldChangeListener;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.time.WorldTime;
import org.terasology.math.geom.Vector3i;

import java.util.Collection;
import java.util.Map;

/**
 *
 */
public class AbstractWorldProviderDecorator implements WorldProviderCore {

    private final WorldProviderCore base;

    public AbstractWorldProviderDecorator(WorldProviderCore base) {
        this.base = base;
    }

    @Override
    public EntityRef getWorldEntity() {
        return base.getWorldEntity();
    }

    @Override
    public String getTitle() {
        return base.getTitle();
    }

    @Override
    public String getSeed() {
        return base.getSeed();
    }

    @Override
    public WorldInfo getWorldInfo() {
        return base.getWorldInfo();
    }

    @Override
    public void processPropagation() {
        base.processPropagation();
    }

    @Override
    public void registerListener(WorldChangeListener listener) {
        base.registerListener(listener);
    }

    @Override
    public void unregisterListener(WorldChangeListener listener) {
        base.unregisterListener(listener);
    }

    @Override
    public ChunkViewCore getLocalView(Vector3i chunkPos) {
        return base.getLocalView(chunkPos);
    }

    @Override
    public ChunkViewCore getWorldViewAround(Vector3i chunk) {
        return base.getWorldViewAround(chunk);
    }

    @Override
    public boolean isBlockRelevant(int x, int y, int z) {
        return base.isBlockRelevant(x, y, z);
    }

    @Override
    public boolean isRegionRelevant(Region3i region) {
        return base.isRegionRelevant(region);
    }

    @Override
    public Block setBlock(Vector3i pos, Block type) {
        return base.setBlock(pos, type);
    }

    @Override
    public Block setBlock(Vector3ic pos, Block type) {
        return base.setBlock(pos, type);
    }

    @Override
    public Map<Vector3i, Block> setBlocks(Map<Vector3i, Block> blocks) {
        return base.setBlocks(blocks);
    }

    @Override
    public Block getBlock(int x, int y, int z) {
        return base.getBlock(x, y, z);
    }

    @Override
    public byte getLight(int x, int y, int z) {
        return base.getLight(x, y, z);
    }

    @Override
    public byte getSunlight(int x, int y, int z) {
        return base.getSunlight(x, y, z);
    }

    @Override
    public byte getTotalLight(int x, int y, int z) {
        return base.getTotalLight(x, y, z);
    }

    @Override
    public int getExtraData(int index, int x, int y, int z) {
        return base.getExtraData(index, x, y, z);
    }

    @Override
    public int setExtraData(int index, Vector3i pos, int value) {
        return base.setExtraData(index, pos, value);
    }

    @Override
    public void dispose() {
        base.dispose();
    }

    @Override
    public WorldTime getTime() {
        return base.getTime();
    }

    @Override
    public Collection<Region3i> getRelevantRegions() {
        return base.getRelevantRegions();
    }

}

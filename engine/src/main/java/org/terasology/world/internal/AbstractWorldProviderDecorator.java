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

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.math.Region3i;
import org.terasology.math.geom.Vector3i;
import org.terasology.world.WorldChangeListener;
import org.terasology.world.biomes.Biome;
import org.terasology.world.block.Block;
import org.terasology.world.liquid.LiquidData;
import org.terasology.world.time.WorldTime;

import java.util.Collection;

/**
 */
public class AbstractWorldProviderDecorator implements WorldProviderCore {

    private WorldProviderCore base;

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
    public boolean setLiquid(int x, int y, int z, LiquidData newState, LiquidData oldState) {
        return base.setLiquid(x, y, z, newState, oldState);
    }

    @Override
    public LiquidData getLiquid(int x, int y, int z) {
        return base.getLiquid(x, y, z);
    }

    @Override
    public Block getBlock(int x, int y, int z) {
        return base.getBlock(x, y, z);
    }

    @Override
    public Biome setBiome(Vector3i pos, Biome biome) {
        return base.setBiome(pos, biome);
    }

    @Override
    public Biome getBiome(Vector3i pos) {
        return base.getBiome(pos);
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

/*
 * Copyright 2013 Moving Blocks
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

package org.terasology.world;

import org.terasology.math.Vector3i;
import org.terasology.world.block.Block;
import org.terasology.world.liquid.LiquidData;

/**
 * @author Immortius
 */
public class AbstractWorldProviderDecorator implements WorldProviderCore {

    private WorldProviderCore base;

    public AbstractWorldProviderDecorator(WorldProviderCore base) {
        this.base = base;
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
    public WorldBiomeProvider getBiomeProvider() {
        return base.getBiomeProvider();
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
    public ChunkView getLocalView(Vector3i chunkPos) {
        return base.getLocalView(chunkPos);
    }

    @Override
    public ChunkView getWorldViewAround(Vector3i chunk) {
        return base.getWorldViewAround(chunk);
    }

    @Override
    public boolean isBlockRelevant(int x, int y, int z) {
        return base.isBlockRelevant(x, y, z);
    }

    @Override
    public boolean setBlocks(BlockUpdate... updates) {
        return base.setBlocks(updates);
    }

    @Override
    public boolean setBlocks(Iterable<BlockUpdate> updates) {
        return base.setBlocks(updates);
    }

    @Override
    public boolean setBlock(int x, int y, int z, Block type, Block oldType) {
        return base.setBlock(x, y, z, type, oldType);
    }

    @Override
    public void setBlockForced(int x, int y, int z, Block type) {
        base.setBlockForced(x, y, z, type);
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
    public long getTime() {
        return base.getTime();
    }

    @Override
    public void setTime(long time) {
        base.setTime(time);
    }

    @Override
    public float getTimeInDays() {
        return base.getTimeInDays();
    }

    @Override
    public void setTimeInDays(float time) {
        base.setTimeInDays(time);
    }

    @Override
    public void dispose() {
        base.dispose();
    }
}

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

import org.terasology.math.Region3i;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.world.WorldChangeListener;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.liquid.LiquidData;

import java.math.RoundingMode;
import java.util.Collection;

/**
 */
public class WorldProviderWrapper extends AbstractWorldProviderDecorator implements WorldProvider {
    private WorldProviderCore core;

    public WorldProviderWrapper(WorldProviderCore core) {
        super(core);
        this.core = core;
    }

    @Override
    public boolean isBlockRelevant(Vector3i pos) {
        return core.isBlockRelevant(pos.x, pos.y, pos.z);
    }

    @Override
    public boolean isBlockRelevant(Vector3f pos) {
        return isBlockRelevant(new Vector3i(pos, RoundingMode.HALF_UP));
    }

    @Override
    public Block setBlock(Vector3i pos, Block type) {
        return core.setBlock(pos, type);
    }

    @Override
    public boolean setLiquid(Vector3i pos, LiquidData state, LiquidData oldState) {
        return core.setLiquid(pos.x, pos.y, pos.z, state, oldState);
    }

    @Override
    public LiquidData getLiquid(Vector3i blockPos) {
        return core.getLiquid(blockPos.x, blockPos.y, blockPos.z);
    }

    @Override
    public Block getBlock(Vector3f pos) {
        return getBlock(new Vector3i(pos, RoundingMode.HALF_UP));
    }

    @Override
    public Block getBlock(Vector3i pos) {
        return core.getBlock(pos.x, pos.y, pos.z);
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
    public Collection<Region3i> getRelevantRegions() {
        return core.getRelevantRegions();
    }
}

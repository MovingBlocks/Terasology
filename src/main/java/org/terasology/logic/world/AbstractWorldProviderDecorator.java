/*
 * Copyright 2012
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

package org.terasology.logic.world;

import org.terasology.math.Vector3i;
import org.terasology.model.blocks.Block;

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
    public WorldView getWorldViewAround(Vector3i chunk) {
        return base.getWorldViewAround(chunk);
    }

    @Override
    public boolean isBlockActive(int x, int y, int z) {
        return base.isBlockActive(x,y,z);
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
        return base.setBlock(x,y,z,type,oldType);
    }

    @Override
    public boolean setState(int x, int y, int z, byte state, byte oldState) {
        return base.setState(x,y,z,state,oldState);
    }

    @Override
    public byte getState(int x, int y, int z) {
        return base.getState(x,y,z);
    }

    @Override
    public Block getBlock(int x, int y, int z) {
        return base.getBlock(x,y,z);
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

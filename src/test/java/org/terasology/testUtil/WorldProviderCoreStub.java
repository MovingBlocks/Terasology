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

package org.terasology.testUtil;

import com.google.common.collect.Maps;
import org.terasology.math.Vector3i;
import org.terasology.world.internal.BlockUpdate;
import org.terasology.world.ChunkView;
import org.terasology.world.WorldBiomeProvider;
import org.terasology.world.WorldChangeListener;
import org.terasology.world.internal.WorldInfo;
import org.terasology.world.WorldProviderCore;
import org.terasology.world.block.Block;
import org.terasology.world.liquid.LiquidData;
import org.terasology.world.time.WorldTime;

import java.util.Map;

/**
 * @author Immortius
 */
public class WorldProviderCoreStub implements WorldProviderCore {

    private Map<Vector3i, Block> blocks = Maps.newHashMap();
    private Block air;

    public WorldProviderCoreStub(Block air) {
        this.air = air;
    }

    @Override
    public String getTitle() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getSeed() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public WorldInfo getWorldInfo() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public WorldBiomeProvider getBiomeProvider() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void registerListener(WorldChangeListener listener) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void unregisterListener(WorldChangeListener listener) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ChunkView getLocalView(Vector3i chunkPos) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ChunkView getWorldViewAround(Vector3i chunk) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isBlockRelevant(int x, int y, int z) {
        return true;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean setBlocks(BlockUpdate... updates) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean setBlocks(Iterable<BlockUpdate> updates) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean setBlock(int x, int y, int z, Block type, Block oldType) {
        Vector3i pos = new Vector3i(x, y, z);
        Block old = blocks.get(pos);
        if (old == null) {
            if (oldType == air) {
                blocks.put(pos, type);
                return true;
            }
        } else if (old == oldType) {
            blocks.put(pos, type);
            return true;
        }
        return false;
    }

    @Override
    public void setBlockForced(int x, int y, int z, Block type) {
        setBlock(x, y, z, type, getBlock(x, y, z));
    }

    @Override
    public boolean setLiquid(int x, int y, int z, LiquidData newData, LiquidData oldData) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public LiquidData getLiquid(int x, int y, int z) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Block getBlock(int x, int y, int z) {
        Block result = blocks.get(new Vector3i(x, y, z));
        if (result == null) {
            return air;
        }
        return result;
    }

    @Override
    public byte getLight(int x, int y, int z) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public byte getSunlight(int x, int y, int z) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public byte getTotalLight(int x, int y, int z) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void dispose() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public WorldTime getTime() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public float getFog(float x, float y, float z) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }
}

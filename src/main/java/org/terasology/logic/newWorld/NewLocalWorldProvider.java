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

package org.terasology.logic.newWorld;

import org.terasology.game.CoreRegistry;
import org.terasology.game.Timer;
import org.terasology.logic.generators.ChunkGeneratorTerrain;
import org.terasology.logic.generators.GeneratorManager;
import org.terasology.logic.manager.Config;
import org.terasology.logic.world.ChunkProvider;
import org.terasology.math.TeraMath;
import org.terasology.math.Vector3i;
import org.terasology.model.blocks.Block;
import org.terasology.model.blocks.management.BlockManager;

import javax.vecmath.Vector3f;

/**
 * @author Immortius
 */
public class NewLocalWorldProvider implements WorldProvider {
    private String title;
    private String seed;

    private WorldBiomeProvider biomeProvider;
    private NewChunkProvider chunkProvider;

    private long timeOffset;

    public NewLocalWorldProvider(String title, String seed, NewChunkProvider chunkProvider) {
        if (seed == null || seed.isEmpty()) {
            throw new IllegalArgumentException("No seed provided.");
        }
        if (title == null) {
            title = seed;
        }

        this.title = title;
        this.seed = seed;
        this.biomeProvider = new WorldBiomeProviderImpl(seed);
        this.chunkProvider = chunkProvider;

        Timer timer = CoreRegistry.get(Timer.class);
        if (timer != null) {
            timeOffset = -timer.getTimeInMs() + Config.getInstance().getInitialTimeOffsetInMs();
        }
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getSeed() {
        return seed;
    }

    @Override
    public WorldBiomeProvider getBiomeProvider() {
        return biomeProvider;
    }

    @Override
    public boolean isBlockActive(int x, int y, int z) {
        return chunkProvider.isChunkAvailable(TeraMath.calcChunkPos(x,y,z));
    }

    @Override
    public boolean isBlockActive(Vector3i pos) {
        return isBlockActive(pos);
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
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean setBlock(Vector3i pos, Block type, Block oldType) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean setState(int x, int y, int z, byte state, byte oldState) {
        Vector3i chunkPos = TeraMath.calcChunkPos(x,y,z);
        NewChunk chunk = chunkProvider.getChunk(chunkPos);
        if (chunk != null) {
            Vector3i blockPos = TeraMath.calcBlockPos(x,y,z, chunkPos);
            return chunk.setState(blockPos, state, oldState);
        }
        return false;
    }

    @Override
    public byte getState(int x, int y, int z) {
        Vector3i chunkPos = TeraMath.calcChunkPos(x,y,z);
        NewChunk chunk = chunkProvider.getChunk(chunkPos);
        if (chunk != null) {
            Vector3i blockPos = TeraMath.calcBlockPos(x,y,z, chunkPos);
            return chunk.getState(blockPos);
        }
        return 0;
    }

    @Override
    public Block getBlock(int x, int y, int z) {
        Vector3i chunkPos = TeraMath.calcChunkPos(x,y,z);
        NewChunk chunk = chunkProvider.getChunk(chunkPos);
        if (chunk != null) {
            Vector3i blockPos = TeraMath.calcBlockPos(x,y,z, chunkPos);
            return chunk.getBlock(blockPos);
        }
        return BlockManager.getInstance().getAir();
    }

    @Override
    public Block getBlock(Vector3f pos) {
        return getBlock(new Vector3i(pos, 0.5f));
    }

    @Override
    public Block getBlock(Vector3i pos) {
        return getBlock(pos.x, pos.y, pos.z);
    }

    @Override
    public byte getLight(int x, int y, int z) {
        Vector3i chunkPos = TeraMath.calcChunkPos(x,y,z);
        NewChunk chunk = chunkProvider.getChunk(chunkPos);
        if (chunk != null) {
            Vector3i blockPos = TeraMath.calcBlockPos(x,y,z, chunkPos);
            return chunk.getLight(blockPos);
        }
        return 0;
    }

    @Override
    public byte getLight(Vector3i pos) {
        return getLight(pos.x, pos.y, pos.z);
    }

    @Override
    public byte getLight(Vector3f pos) {
        return getLight(new Vector3i(pos, 0.5f));
    }

    @Override
    public byte getSunlight(int x, int y, int z) {
        Vector3i chunkPos = TeraMath.calcChunkPos(x,y,z);
        NewChunk chunk = chunkProvider.getChunk(chunkPos);
        if (chunk != null) {
            Vector3i blockPos = TeraMath.calcBlockPos(x,y,z, chunkPos);
            return chunk.getSunlight(blockPos);
        }
        return 0;
    }

    @Override
    public byte getTotalLight(int x, int y, int z) {
        Vector3i chunkPos = TeraMath.calcChunkPos(x,y,z);
        NewChunk chunk = chunkProvider.getChunk(chunkPos);
        if (chunk != null) {
            Vector3i blockPos = TeraMath.calcBlockPos(x,y,z, chunkPos);
            return (byte)Math.max(chunk.getSunlight(blockPos), chunk.getLight(blockPos));
        }
        return 0;
    }


    @Override
    public byte getSunlight(Vector3f pos) {
        return getSunlight(new Vector3i(pos, 0.5f));
    }

    @Override
    public byte getTotalLight(Vector3f pos) {
        return getTotalLight(new Vector3i(pos, 0.5f));
    }


    @Override
    public byte getSunlight(Vector3i pos) {
        return getSunlight(pos.x, pos.y, pos.z);
    }

    @Override
    public byte getTotalLight(Vector3i pos) {
        return getTotalLight(pos.x, pos.y, pos.z);
    }

    @Override
    public long getTime() {
        return CoreRegistry.get(Timer.class).getTimeInMs() + timeOffset;
    }

    @Override
    public void setTime(long time) {
        timeOffset = time - CoreRegistry.get(Timer.class).getTimeInMs();
    }

    @Override
    public void dispose() {
        chunkProvider.dispose();
    }
}

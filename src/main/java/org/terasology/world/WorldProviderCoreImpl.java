/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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

import org.terasology.config.Config;
import org.terasology.config.ModConfig;
import org.terasology.game.CoreRegistry;
import org.terasology.game.Timer;
import org.terasology.game.types.GameType;
import org.terasology.logic.mod.Mod;
import org.terasology.logic.mod.ModManager;
import org.terasology.math.Region3i;
import org.terasology.math.TeraMath;
import org.terasology.math.Vector3i;
import org.terasology.world.block.Block;
import org.terasology.world.block.management.BlockManager;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.ChunkProvider;
import org.terasology.world.lighting.LightPropagator;
import org.terasology.world.lighting.LightingUtil;
import org.terasology.world.lighting.PropagationComparison;
import org.terasology.world.liquid.LiquidData;

/**
 * @author Immortius
 */
public class WorldProviderCoreImpl implements WorldProviderCore {
    private final long DAY_NIGHT_LENGTH_IN_MS = CoreRegistry.get(Config.class).getSystem().getDayNightLengthInMs();

    private String title;
    private String seed;
    private String[] chunkGenerators;

    private WorldBiomeProvider biomeProvider;
    private ChunkProvider chunkProvider;

    private long timeOffset;

    public WorldProviderCoreImpl(String title, String seed, long time, String[] chunkGenerators, ChunkProvider chunkProvider) {
        if (seed == null || seed.isEmpty()) {
            throw new IllegalArgumentException("No seed provided.");
        }
        if (title == null) {
            title = seed;
        }

        this.title = title;
        this.seed = seed;
        this.chunkGenerators = chunkGenerators;
        this.biomeProvider = new WorldBiomeProviderImpl(seed);
        this.chunkProvider = chunkProvider;
        setTime(time);
    }

    public WorldProviderCoreImpl(WorldInfo info, ChunkProvider chunkProvider) {
        this(info.getTitle(), info.getSeed(), info.getTime(), info.getChunkGenerators(), chunkProvider);
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
    public WorldInfo getWorldInfo() {
        ModConfig modConfig = new ModConfig();
        for (Mod mod : CoreRegistry.get(ModManager.class).getActiveMods()) {
            modConfig.addMod(mod.getModInfo().getId());
        }
        WorldInfo worldInfo = new WorldInfo(title, seed, getTime(), chunkGenerators, CoreRegistry.get(GameType.class).getClass().toString(), modConfig);
        worldInfo.setBlockIdMap(BlockManager.getInstance().getBlockIdMap());
        return worldInfo;
    }

    @Override
    public WorldBiomeProvider getBiomeProvider() {
        return biomeProvider;
    }

    @Override
    public WorldView getLocalView(Vector3i chunk) {
        return WorldView.createLocalView(chunk, chunkProvider);
    }

    @Override
    public WorldView getWorldViewAround(Vector3i chunk) {
        return WorldView.createSubviewAroundChunk(chunk, chunkProvider);
    }

    @Override
    public boolean isBlockActive(int x, int y, int z) {
        return chunkProvider.isChunkAvailable(TeraMath.calcChunkPos(x, y, z));
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
        Vector3i blockPos = new Vector3i(x, y, z);
        WorldView worldView;

        if (LightingUtil.compareLightingPropagation(type, oldType) != PropagationComparison.IDENTICAL || type.getLuminance() != oldType.getLuminance()) {
            worldView = WorldView.createSubviewAroundBlock(blockPos, Chunk.MAX_LIGHT + 1, chunkProvider);
        } else {
            worldView = WorldView.createSubviewAroundBlock(blockPos, 1, chunkProvider);
        }
        if (worldView != null) {
            worldView.lock();
            try {
                if (!worldView.setBlock(x, y, z, type, oldType)) {
                    return false;
                }

                Region3i affected = new LightPropagator(worldView).update(x, y, z, type, oldType);
                if (affected.isEmpty()) {
                    worldView.setDirtyAround(blockPos);
                } else {
                    worldView.setDirtyAround(affected);
                }
                return true;
            } finally {
                worldView.unlock();
            }
        }
        return false;
    }

    @Override
    public boolean setLiquid(int x, int y, int z, LiquidData newState, LiquidData oldState) {
        // TODO: Locking, light changes
        Vector3i chunkPos = TeraMath.calcChunkPos(x, y, z);
        Chunk chunk = chunkProvider.getChunk(chunkPos);
        if (chunk != null) {
            Vector3i blockPos = TeraMath.calcBlockPos(x, y, z);
            return chunk.setLiquid(blockPos, newState, oldState);
        }
        return false;
    }

    @Override
    public LiquidData getLiquid(int x, int y, int z) {
        y = TeraMath.clamp(y, 0, Chunk.SIZE_Y - 1);

        Vector3i chunkPos = TeraMath.calcChunkPos(x, y, z);
        Chunk chunk = chunkProvider.getChunk(chunkPos);
        if (chunk != null) {
            Vector3i blockPos = TeraMath.calcBlockPos(x, y, z);
            return chunk.getLiquid(blockPos);
        }
        return new LiquidData();
    }

    @Override
    public Block getBlock(int x, int y, int z) {
        if (y >= Chunk.SIZE_Y || y < 0) {
            return BlockManager.getInstance().getAir();
        }

        Vector3i chunkPos = TeraMath.calcChunkPos(x, y, z);
        Chunk chunk = chunkProvider.getChunk(chunkPos);
        if (chunk != null) {
            Vector3i blockPos = TeraMath.calcBlockPos(x, y, z);
            return chunk.getBlock(blockPos);
        }
        return BlockManager.getInstance().getAir();
    }

    @Override
    public byte getLight(int x, int y, int z) {
        y = TeraMath.clamp(y, 0, Chunk.SIZE_Y - 1);

        Vector3i chunkPos = TeraMath.calcChunkPos(x, y, z);
        Chunk chunk = chunkProvider.getChunk(chunkPos);
        if (chunk != null) {
            Vector3i blockPos = TeraMath.calcBlockPos(x, y, z);
            return chunk.getLight(blockPos);
        }
        return 0;
    }

    @Override
    public byte getSunlight(int x, int y, int z) {
        y = TeraMath.clamp(y, 0, Chunk.SIZE_Y - 1);

        Vector3i chunkPos = TeraMath.calcChunkPos(x, y, z);
        Chunk chunk = chunkProvider.getChunk(chunkPos);
        if (chunk != null) {
            Vector3i blockPos = TeraMath.calcBlockPos(x, y, z);
            return chunk.getSunlight(blockPos);
        }
        return 0;
    }

    @Override
    public byte getTotalLight(int x, int y, int z) {
        y = TeraMath.clamp(y, 0, Chunk.SIZE_Y - 1);

        Vector3i chunkPos = TeraMath.calcChunkPos(x, y, z);
        Chunk chunk = chunkProvider.getChunk(chunkPos);
        if (chunk != null) {
            Vector3i blockPos = TeraMath.calcBlockPos(x, y, z);
            return (byte) Math.max(chunk.getSunlight(blockPos), chunk.getLight(blockPos));
        }
        return 0;
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
    public float getTimeInDays() {
        return (float) getTime() / DAY_NIGHT_LENGTH_IN_MS;
    }

    @Override
    public void setTimeInDays(float time) {
        setTime((long) (time * DAY_NIGHT_LENGTH_IN_MS));
    }

    @Override
    public void dispose() {
        chunkProvider.dispose();
    }
}

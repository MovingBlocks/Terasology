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
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.math.Region3i;
import org.terasology.math.geom.Vector3i;
import org.terasology.world.WorldChangeListener;
import org.terasology.world.biomes.Biome;
import org.terasology.world.biomes.BiomeManager;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.internal.ChunkViewCore;
import org.terasology.world.internal.WorldInfo;
import org.terasology.world.internal.WorldProviderCore;
import org.terasology.world.liquid.LiquidData;
import org.terasology.world.time.WorldTime;
import org.terasology.world.time.WorldTimeImpl;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 */
public class WorldProviderCoreStub implements WorldProviderCore {

    private Map<Vector3i, Block> blocks = Maps.newHashMap();
    private Map<Vector3i, Biome> biomes = Maps.newHashMap();
    private Block air;
    private Biome defaultBiome;

    public WorldProviderCoreStub(Block air, Biome defaultBiome) {
        this.air = air;
        this.defaultBiome = defaultBiome;
    }

    @Override
    public EntityRef getWorldEntity() {
        return EntityRef.NULL;
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
    public void processPropagation() {
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
    public ChunkViewCore getLocalView(Vector3i chunkPos) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ChunkViewCore getWorldViewAround(Vector3i chunk) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isBlockRelevant(int x, int y, int z) {
        return true;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isRegionRelevant(Region3i region) {
        return true;
    }

    @Override
    public Block setBlock(Vector3i pos, Block type) {
        Block old = blocks.put(pos, type);
        if (old == null) {
            return air;
        }
        return old;
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
    public Biome setBiome(Vector3i pos, Biome biome) {
        Biome oldBiome = biomes.put(pos, biome);
        if (oldBiome == null) {
            return defaultBiome;
        }
        return oldBiome;
    }

    @Override
    public Biome getBiome(Vector3i pos) {
        Biome result = biomes.get(pos);
        if (result == null) {
            return defaultBiome;
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
        return new WorldTimeImpl();  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Collection<Region3i> getRelevantRegions() {
        return Collections.emptySet();
    }


}

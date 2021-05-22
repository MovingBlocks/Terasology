// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.testUtil;

import com.google.common.collect.Maps;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.world.WorldChangeListener;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockRegionc;
import org.terasology.engine.world.internal.ChunkViewCore;
import org.terasology.engine.world.internal.WorldInfo;
import org.terasology.engine.world.internal.WorldProviderCore;
import org.terasology.engine.world.time.WorldTime;
import org.terasology.engine.world.time.WorldTimeImpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 */
public class WorldProviderCoreStub implements WorldProviderCore {

    private Map<Vector3ic, Block> blocks = Maps.newHashMap();
    private ArrayList<Map<Vector3ic, Integer>> extraData = new ArrayList<>();
    private Block air;

    public WorldProviderCoreStub(Block air) {
        this.air = air;
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
    public ChunkViewCore getLocalView(Vector3ic chunkPos) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ChunkViewCore getWorldViewAround(Vector3ic chunk) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ChunkViewCore getWorldViewAround(BlockRegionc chunk) {
        return null;
    }

    @Override
    public boolean isBlockRelevant(int x, int y, int z) {
        return true;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isRegionRelevant(BlockRegionc region) {
        return false;
    }

    @Override
    public Block setBlock(Vector3ic pos, Block type) {
        Block old = blocks.put(new Vector3i(pos), type);
        if (old == null) {
            return air;
        }
        return old;
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
    public int setExtraData(int index, Vector3ic pos, int value) {
        Integer prevValue = getExtraDataLayer(index).put(new Vector3i(pos), value);
        return prevValue == null ? 0 : prevValue;
    }

    @Override
    public int getExtraData(int index, int x, int y, int z) {
        return getExtraDataLayer(index).getOrDefault(new Vector3i(x, y, z), 0);
    }

    private Map<Vector3ic, Integer> getExtraDataLayer(int index) {
        while (extraData.size() <= index) {
            extraData.add(Maps.newHashMap());
        }
        return extraData.get(index);
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
    public Collection<BlockRegionc> getRelevantRegions() {
        return Collections.emptySet();
    }


}

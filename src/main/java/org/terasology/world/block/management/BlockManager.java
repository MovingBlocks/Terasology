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
package org.terasology.world.block.management;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import gnu.trove.iterator.TObjectByteIterator;
import gnu.trove.map.hash.TByteObjectHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectByteHashMap;
import org.lwjgl.BufferUtils;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockPart;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.family.BlockFamily;

import javax.vecmath.Vector2f;
import java.nio.FloatBuffer;
import java.util.List;
import java.util.Map;

/**
 * Provides access to blocks by block id or block title.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class BlockManager {

    /* SINGLETON */
    private static BlockManager instance;

    /* BLOCKS */
    private final Map<BlockUri, Block> blocksByUri = Maps.newHashMapWithExpectedSize(256);
    private final TByteObjectHashMap<Block> blocksById = new TByteObjectHashMap<Block>(256);

    private int nextId = 1;
    private final TObjectByteHashMap<BlockUri> idByUri = new TObjectByteHashMap<BlockUri>(256);
    private final Map<BlockUri, BlockFamily> familyByUri = Maps.newHashMapWithExpectedSize(128);

    public static BlockManager getInstance() {
        if (instance == null)
            instance = new BlockManager();

        return instance;
    }

    private BlockManager() {
        reset();
    }

    public void reset() {
        blocksById.clear();
        blocksByUri.clear();
        familyByUri.clear();
        nextId = 1;

        Block air = new Block();
        air.setTranslucent(true);
        air.setInvisible(true);
        air.setTargetable(false);
        air.setPenetrable(false);
        air.setShadowCasting(false);
        air.setAttachmentAllowed(false);
        air.setHardness((byte) 0);
        air.setId((byte) 0);
        air.setDisplayName("Air");
        air.setUri(new BlockUri("engine", "air"));
        blocksById.put(air.getId(), air);
        blocksByUri.put(air.getURI(), air);
        idByUri.put(air.getURI(), air.getId());
    }

    public void setBlockIdMap(Map<String, Byte> blockUris) {
        for (Map.Entry<String, Byte> entry : blockUris.entrySet()) {
            idByUri.put(new BlockUri(entry.getKey()), (byte) entry.getValue());
        }
        nextId = blockUris.size();
    }

    public Map<String, Byte> getBlockIdMap() {
        Map<String, Byte> result = Maps.newHashMapWithExpectedSize(idByUri.size());
        TObjectByteIterator<BlockUri> iterator = idByUri.iterator();
        while(iterator.hasNext()) {
            result.put(iterator.key().toString(), iterator.value());
        }
        return result;
    }

    public BlockFamily getBlockFamily(String uri) {
        return familyByUri.get(new BlockUri(uri));
    }

    public BlockFamily getBlockFamily(BlockUri uri) {
        return familyByUri.get(uri);
    }

    public Block getBlock(String uri) {
        return getBlock(new BlockUri(uri));
    }

    public Block getBlock(BlockUri uri) {
        Block block = blocksByUri.get(uri);
        if (block == null) {
            return blocksById.get((byte) 0);
        }
        return block;
    }

    public Block getBlock(byte id) {
        Block result = blocksById.get(id);
        if (result == null)
            return blocksById.get((byte) 0);
        return result;
    }

    public Block getAir() {
        return blocksById.get((byte) 0);
    }

    public void addAllBlockFamilies(Iterable<BlockFamily> families) {
        for (BlockFamily family : families) {
            addBlockFamily(family);
        }
    }

    public void addBlockFamily(BlockFamily family) {
        familyByUri.put(family.getURI(), family);
        for (Block block : family.listBlocks()) {
            byte id = idByUri.get(block.getURI());
            if (id == 0) {
                id = (byte)nextId++;
                idByUri.put(block.getURI(), id);
            }
            block.setId(id);
            blocksById.put(block.getId(), block);
            blocksByUri.put(block.getURI(), block);
        }
    }

    public Iterable<BlockFamily> listBlockFamilies() {
        return familyByUri.values();
    }

    public int getBlockFamilyCount() {
        return familyByUri.size();
    }

    public FloatBuffer calcCoordinatesForWavingBlocks() {
        FloatBuffer buffer = BufferUtils.createFloatBuffer(32);

        int counter = 0;
        for (Block b : blocksByUri.values()) {
            if (b.isWaving()) {
                Vector2f pos = b.getTextureAtlasPos(BlockPart.TOP);
                buffer.put(pos.x);
                buffer.put(pos.y);
                counter++;
            }
        }

        while (counter < 16) {
            buffer.put(-1);
            buffer.put(-1);
            counter++;
        }

        buffer.flip();
        return buffer;
    }

    public FloatBuffer calcCoordinate(String uri) {
        BlockUri blockUri = new BlockUri(uri);
        FloatBuffer buffer = BufferUtils.createFloatBuffer(2);

        if (blocksByUri.containsKey(blockUri)) {
            Vector2f position = blocksByUri.get(blockUri).getTextureAtlasPos(BlockPart.LEFT);
            buffer.put(position.x);
            buffer.put(position.y);
        }

        buffer.flip();
        return buffer;
    }
}

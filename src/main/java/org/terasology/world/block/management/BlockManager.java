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

import gnu.trove.iterator.TObjectByteIterator;
import gnu.trove.map.hash.TByteObjectHashMap;
import gnu.trove.map.hash.TObjectByteHashMap;

import java.nio.FloatBuffer;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.vecmath.Vector2f;

import org.lwjgl.BufferUtils;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockPart;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.family.BlockFamily;
import org.terasology.world.block.loader.BlockLoader;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * Provides access to blocks by block id or block title.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class BlockManager {

    /* SINGLETON */
    private static BlockManager instance;
    private Logger logger = Logger.getLogger(getClass().getName());

    private BlockLoader blockLoader;

    /* BLOCKS */
    private final Map<BlockUri, Block> blocksByUri = Maps.newHashMapWithExpectedSize(256);
    private final TByteObjectHashMap<Block> blocksById = new TByteObjectHashMap<Block>(256);

    private int nextId = 1;
    private final TObjectByteHashMap<BlockUri> idByUri = new TObjectByteHashMap<BlockUri>(256);

    /* Families */
    private final Set<BlockUri> shapelessBlockDefinition = Sets.newHashSet();
    private final Map<BlockUri, BlockFamily> partiallyRegisteredFamilies = Maps.newHashMap();
    private final Map<BlockUri, BlockFamily> familyByUri = Maps.newHashMapWithExpectedSize(128);

    public static BlockManager getInstance() {
        if (instance == null)
            instance = new BlockManager();

        return instance;
    }

    private BlockManager() {
        blockLoader = new BlockLoader();
        reset();
    }

    public void reset() {
        blocksById.clear();
        blocksByUri.clear();
        familyByUri.clear();
        partiallyRegisteredFamilies.clear();
        shapelessBlockDefinition.clear();
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

    public void load(Map<String, Byte> knownBlockMappings) {
        reset();
        for (Map.Entry<String, Byte> entry : knownBlockMappings.entrySet()) {
            idByUri.put(new BlockUri(entry.getKey()), (byte) entry.getValue());
        }
        nextId = idByUri.size();

        BlockLoader.LoadBlockDefinitionResults blockDefinitions = blockLoader.loadBlockDefinitions();
        for (BlockFamily family : blockDefinitions.families) {
            registerBlockFamily(family);
        }
        for (BlockUri shapelessFamily : blockDefinitions.shapelessDefinitions) {
            shapelessBlockDefinition.add(shapelessFamily);
        }
        blockLoader.buildAtlas();
        bindBlocks(knownBlockMappings);
    }

    private void bindBlocks(Map<String, Byte> knownBlockMappings) {
        for (String blockUri : knownBlockMappings.keySet()) {
            Block block = getBlock(new BlockUri(blockUri));
            if (block == null) {
                logger.warning("Block " + blockUri + " no longer available");
            }
        }
    }

    public Map<String, Byte> getBlockIdMap() {
        Map<String, Byte> result = Maps.newHashMapWithExpectedSize(idByUri.size());
        TObjectByteIterator<BlockUri> iterator = idByUri.iterator();
        while (iterator.hasNext()) {
            iterator.advance();
            result.put(iterator.key().toString(), iterator.value());
        }
        return result;
    }

    public BlockFamily getBlockFamily(String uri) {
        return getBlockFamily(new BlockUri(uri));
    }

    public BlockFamily getBlockFamily(BlockUri uri) {
        BlockFamily family = familyByUri.get(uri);
        if (family == null) {
            family = partiallyRegisteredFamilies.get(uri);
            if (family != null) {
                partiallyRegisteredFamilies.remove(uri);
                registerBlockFamily(family);
            } else {
                BlockUri shapelessUri = new BlockUri(uri.getPackage(), uri.getFamily());
                if (shapelessBlockDefinition.contains(shapelessUri)) {
                    family = blockLoader.loadWithShape(uri);
                    if (family != null) {
                        registerBlockFamily(family);
                    } else {
                        logger.severe("Failed to load shapeless def: " + uri);
                    }
                }
            }
        }
        return family;
    }

    public Block getBlock(String uri) {
        return getBlock(new BlockUri(uri));
    }

    public Block getBlock(BlockUri uri) {
        Block block = blocksByUri.get(uri);
        if (block == null) {
            // Check if partially registered by getting the block family
            BlockFamily family = getBlockFamily(uri.getFamilyUri());
            if (family != null) {
                block = family.getBlockFor(uri);
            }
            if (block == null) {
                return blocksById.get((byte) 0);
            }
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

    public void addBlockFamily(BlockFamily family) {
        partiallyRegisteredFamilies.put(family.getURI(), family);
    }

    public void addShapelessBlockFamily(BlockUri family) {
        shapelessBlockDefinition.add(family);
    }

    private void registerBlockFamily(BlockFamily family) {
        familyByUri.put(family.getURI(), family);
        for (Block block : family.listBlocks()) {
            byte id = idByUri.get(block.getURI());
            if (id == 0) {
                id = (byte) nextId++;
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
        for (BlockFamily b : familyByUri.values()) {
            if (b.getArchetypeBlock().isWaving()) {
                // TODO: Don't use random block part
                Vector2f pos = b.getArchetypeBlock().getTextureAtlasPos(BlockPart.TOP);
                buffer.put(pos.x);
                buffer.put(pos.y);
                counter++;
            }
        }
        for (BlockFamily b : partiallyRegisteredFamilies.values()) {
            if (b.getArchetypeBlock().isWaving()) {
                // TODO: Don't use random block part
                Vector2f pos = b.getArchetypeBlock().getTextureAtlasPos(BlockPart.TOP);
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
        Block block = getBlock(blockUri);
        FloatBuffer buffer = BufferUtils.createFloatBuffer(2);

        if (!block.isInvisible()) {
            // TODO: Don't use random block part
            Vector2f position = block.getTextureAtlasPos(BlockPart.LEFT);
            buffer.put(position.x);
            buffer.put(position.y);
        }

        buffer.flip();
        return buffer;
    }

    public boolean hasBlockFamily(BlockUri uri) {
        return familyByUri.containsKey(uri) || partiallyRegisteredFamilies.containsKey(uri);
    }
}

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

package org.terasology.world.block.management;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.MapMaker;
import com.google.common.collect.Maps;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import gnu.trove.iterator.TObjectByteIterator;
import gnu.trove.map.hash.TByteObjectHashMap;
import gnu.trove.map.hash.TObjectByteHashMap;
import org.lwjgl.BufferUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.asset.Assets;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockPart;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.family.BlockFamily;
import org.terasology.world.block.loader.BlockLoader;
import org.terasology.world.block.loader.FreeformFamily;

import javax.vecmath.Vector2f;
import java.nio.FloatBuffer;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * @author Immortius
 */
public class BlockManagerImpl extends BlockManager {

    private static final Logger logger = LoggerFactory.getLogger(BlockManagerImpl.class);
    private static final int NUM_WAVING_TEXTURES = 16;

    // This is the id we assign to blocks whose mappings are missing. This shouldn't happen, but in case it does
    // we set them to the last id (don't want to use 0 as they would override air)
    private static final byte UNKNOWN_ID = (byte) 255;
    private static final int MAX_ID = 255;

    /* Families */
    private final Set<BlockUri> freeformBlockUris = Sets.newHashSet();
    private final SetMultimap<String, BlockUri> categoryLookup = HashMultimap.create();
    private final Map<BlockUri, BlockFamily> availableFamilies = Maps.newHashMap();
    private final Map<BlockUri, BlockFamily> registeredFamilyByUri = Maps.newHashMapWithExpectedSize(128);

    /* Blocks */
    private final Map<BlockUri, Block> blocksByUri = Maps.newHashMapWithExpectedSize(256);
    private final TByteObjectHashMap<Block> blocksById = new TByteObjectHashMap<>(256);
    private final TObjectByteHashMap<BlockUri> idByUri = new TObjectByteHashMap<>(256);

    private Set<BlockRegistrationListener> listeners = Sets.newSetFromMap(new MapMaker().weakKeys().<BlockRegistrationListener, Boolean>makeMap());

    private BlockLoader blockLoader;

    private boolean generateNewIds = false;
    private int nextId = 1;

    public BlockManagerImpl() {
        this(Lists.<String>newArrayList(), Maps.<String, Byte>newHashMap(), true);
    }

    public BlockManagerImpl(List<String> registeredBlockFamilies, Map<String, Byte> knownBlockMappings, boolean generateNewIds) {
        this.generateNewIds = generateNewIds;
        blockLoader = new BlockLoader();
        BlockLoader.LoadBlockDefinitionResults blockDefinitions = blockLoader.loadBlockDefinitions();
        addBlockFamily(getAirFamily(), true);
        for (BlockFamily family : blockDefinitions.families) {
            addBlockFamily(family, false);
        }
        for (FreeformFamily freeformFamily : blockDefinitions.shapelessDefinitions) {
            addFreeformBlockFamily(freeformFamily.uri, freeformFamily.categories);
        }
        if (knownBlockMappings.size() >= MAX_ID) {
            nextId = UNKNOWN_ID;
        } else if (knownBlockMappings.size() > 0) {
            nextId = (byte) knownBlockMappings.size();
        }

        for (String rawFamilyUri : registeredBlockFamilies) {
            BlockUri familyUri = new BlockUri(rawFamilyUri);
            BlockFamily family;
            if (isFreeformFamily(familyUri)) {
                family = blockLoader.loadWithShape(familyUri);
            } else {
                family = getAvailableBlockFamily(familyUri);
            }
            if (family != null) {
                for (Block block : family.getBlocks()) {
                    Byte id = knownBlockMappings.get(block.getURI().toString());
                    if (id != null) {
                        block.setId(id);
                    } else {
                        logger.error("Missing id for block {} in provided family {}", block.getURI(), family.getURI());
                        if (generateNewIds) {
                            block.setId(getNextId());
                        } else {
                            block.setId(UNKNOWN_ID);
                        }
                    }
                }
                registerFamily(family);
            } else {
                logger.error("Family not available: {}", rawFamilyUri);
            }
        }
    }

    private byte getNextId() {
        if (nextId >= MAX_ID) {
            return UNKNOWN_ID;
        }
        return (byte)nextId++;
    }

    public void subscribe(BlockRegistrationListener listener) {
        this.listeners.add(listener);
    }

    public void unsubscribe(BlockRegistrationListener listener) {
        this.listeners.remove(listener);
    }

    public void receiveFamilyRegistration(BlockUri familyUri, Map<String, Integer> registration) {
        BlockFamily family;
        if (isFreeformFamily(familyUri.getRootFamilyUri())) {
            family = blockLoader.loadWithShape(familyUri);
        } else {
            family = getAvailableBlockFamily(familyUri);
        }
        if (family != null) {
            for (Block block : family.getBlocks()) {
                Integer id = registration.get(block.getURI().toString());
                if (id != null) {
                    block.setId((byte)id.intValue());
                } else {
                    logger.error("Missing id for block {} in registered family {}", block.getURI(), familyUri);
                    block.setId(UNKNOWN_ID);
                }
                registerFamily(family);
            }
        } else {
            logger.error("Block family not available: {}", familyUri);
        }
    }

    public void buildAtlas() {
        blockLoader.buildAtlas();
    }

    /**
     * @param family
     * @param andRegister Immediately registers the family - it is expected that the blocks have been given ids.
     */
    @VisibleForTesting
    public void addBlockFamily(BlockFamily family, boolean andRegister) {
        for (String category : family.getCategories()) {
            categoryLookup.put(category, family.getURI());
        }
        availableFamilies.put(family.getURI(), family);
        if (andRegister) {
            registerFamily(family);
        }
    }

    @VisibleForTesting
    public void addFreeformBlockFamily(BlockUri family, String... categories) {
        freeformBlockUris.add(family);
        for (String category : categories) {
            categoryLookup.put(category, family);
        }
    }

    @VisibleForTesting
    protected void registerFamily(BlockFamily family) {
        logger.info("Registered {}", family);
        registeredFamilyByUri.put(family.getURI(), family);
        for (Block block : family.getBlocks()) {
            registerBlock(block);
        }
        for (BlockRegistrationListener listener : listeners) {
            listener.onBlockFamilyRegistered(family);
        }
    }

    private void registerBlock(Block block) {
        if (block.getId() != UNKNOWN_ID) {
            logger.info("Registered Block {} with id {}", block, block.getId());
            blocksById.put(block.getId(), block);
            idByUri.put(block.getURI(), block.getId());
        } else {
            logger.info("Failed to register block {} - no id", block, block.getId());
        }
        blocksByUri.put(block.getURI(), block);
    }

    /**
     * Retrieve all {@code BlockUri}s that match the given string.
     * <p/>
     * In order to resolve the {@code BlockUri}s, every package is searched for the given uri pattern.
     *
     * @param uri the uri pattern to match
     * @return a list of matching block uris
     */
    @Override
    public List<BlockUri> resolveBlockUri(String uri) {
        List<BlockUri> matches = Lists.newArrayList();
        BlockUri straightUri = new BlockUri(uri);
        if (straightUri.isValid()) {
            if (hasBlockFamily(straightUri)) {
                matches.add(straightUri);
            }
        } else {
            for (String packageName : Assets.listModules()) {
                BlockUri modUri = new BlockUri(packageName, uri);
                if (hasBlockFamily(modUri)) {
                    matches.add(modUri);
                }
            }
        }
        return matches;
    }

    @Override
    public Map<String, Byte> getBlockIdMap() {
        Map<String, Byte> result = Maps.newHashMapWithExpectedSize(idByUri.size());
        TObjectByteIterator<BlockUri> iterator = idByUri.iterator();
        while (iterator.hasNext()) {
            iterator.advance();
            result.put(iterator.key().toString(), iterator.value());
        }
        return result;
    }

    @Override
    public Iterable<BlockUri> getBlockFamiliesWithCategory(String category) {
        return categoryLookup.get(category.toLowerCase(Locale.ENGLISH));
    }

    @Override
    public Iterable<String> getBlockCategories() {
        return categoryLookup.keySet();
    }

    @Override
    public BlockFamily getBlockFamily(String uri) {
        return getBlockFamily(new BlockUri(uri));
    }

    @Override
    public BlockFamily getBlockFamily(BlockUri uri) {
        BlockFamily family = registeredFamilyByUri.get(uri);
        if (family == null && generateNewIds) {
            if (isFreeformFamily(uri.getRootFamilyUri())) {
                family = blockLoader.loadWithShape(uri);
            } else {
                family = getAvailableBlockFamily(uri);
            }
            if (family != null) {
                for (Block block : family.getBlocks()) {
                    block.setId(getNextId());
                }
                registerFamily(family);
            }
        }
        return family;
    }

    @Override
    public Block getBlock(String uri) {
        return getBlock(new BlockUri(uri));
    }

    @Override
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

    @Override
    public Block getBlock(byte id) {
        Block result = blocksById.get(id);
        if (result == null) {
            return blocksById.get((byte) 0);
        }
        return result;
    }

    @Override
    public Iterable<BlockUri> listRegisteredBlockUris() {
        return registeredFamilyByUri.keySet();
    }

    @Override
    public Iterable<BlockFamily> listRegisteredBlockFamilies() {
        return registeredFamilyByUri.values();
    }

    @Override
    public int registeredBlockFamiliesCount() {
        return registeredFamilyByUri.size();
    }

    @Override
    public Iterable<BlockUri> listFreeformBlockUris() {
        return freeformBlockUris;
    }

    @Override
    public boolean isFreeformFamily(BlockUri familyUri) {
        return freeformBlockUris.contains(familyUri);
    }

    @Override
    public Iterable<BlockFamily> listAvailableBlockFamilies() {
        return availableFamilies.values();
    }

    @Override
    public BlockFamily getAvailableBlockFamily(BlockUri uri) {
        return availableFamilies.get(uri);
    }

    @Override
    public Iterable<BlockUri> listAvailableBlockUris() {
        return availableFamilies.keySet();
    }

    @Override
    public int getBlockFamilyCount() {
        return registeredFamilyByUri.size();
    }

    @Override
    public FloatBuffer calcCoordinatesForWavingBlocks() {
        FloatBuffer buffer = BufferUtils.createFloatBuffer(NUM_WAVING_TEXTURES * 2);

        int counter = 0;
        for (BlockFamily b : registeredFamilyByUri.values()) {
            if (b.getArchetypeBlock().isWaving()) {
                // TODO: Don't use random block part
                Vector2f pos = b.getArchetypeBlock().getTextureAtlasPos(BlockPart.TOP);
                buffer.put(pos.x);
                buffer.put(pos.y);
                counter++;
            }
            if (counter >= NUM_WAVING_TEXTURES) {
                break;
            }
        }

        while (counter < NUM_WAVING_TEXTURES) {
            buffer.put(-1);
            buffer.put(-1);
            counter++;
        }

        buffer.flip();
        return buffer;
    }

    @Override
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

    @Override
    public boolean hasBlockFamily(BlockUri uri) {
        return registeredFamilyByUri.containsKey(uri) || availableFamilies.containsKey(uri) || freeformBlockUris.contains(uri);
    }
}

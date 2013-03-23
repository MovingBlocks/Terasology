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
import org.terasology.logic.mod.ModManager;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockPart;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.family.BlockFamily;
import org.terasology.world.block.family.SymmetricFamily;
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
public abstract class BlockManager {

    private static final Logger logger = LoggerFactory.getLogger(BlockManager.class);

    private static final int NUM_WAVING_TEXTURES = 16;

    private final static Block AIR;
    private final static BlockFamily AIR_FAMILY;

    public static final String AIR_ID = "air";

    static {
        AIR = new Block();
        AIR.setTranslucent(true);
        AIR.setInvisible(true);
        AIR.setTargetable(false);
        AIR.setPenetrable(true);
        AIR.setReplacementAllowed(true);
        AIR.setShadowCasting(false);
        AIR.setAttachmentAllowed(false);
        AIR.setHardness((byte) 0);
        AIR.setId((byte) 0);
        AIR.setDisplayName("Air");
        AIR.setUri(new BlockUri(ModManager.ENGINE_PACKAGE, AIR_ID));
        AIR_FAMILY = new SymmetricFamily(AIR.getURI(), AIR);
    }

    public static Block getAir() {
        return AIR;
    }

    public static BlockFamily getAirFamily() {
        return AIR_FAMILY;
    }

    /* Families */
    private final Set<BlockUri> freeformBlockUris = Sets.newHashSet();
    private final SetMultimap<String, BlockUri> categoryLookup = HashMultimap.create();
    private final Map<BlockUri, BlockFamily> availableFamilies = Maps.newHashMap();
    private final Map<BlockUri, BlockFamily> registeredFamilyByUri = Maps.newHashMapWithExpectedSize(128);

    /* Blocks */
    private final Map<BlockUri, Block> blocksByUri = Maps.newHashMapWithExpectedSize(256);
    private final TByteObjectHashMap<Block> blocksById = new TByteObjectHashMap<Block>(256);
    private final TObjectByteHashMap<BlockUri> idByUri = new TObjectByteHashMap<BlockUri>(256);

    private List<BlockRegistrationListener> listeners = Lists.newArrayList();

    public void subscribe(BlockRegistrationListener listener) {
        this.listeners.add(listener);
    }

    public void unsubscribe(BlockRegistrationListener listener) {
        listeners.remove(listener);
    }

    /**
     *
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
        logger.info("Registered Block {} with id {}", block, block.getId());
        blocksById.put(block.getId(), block);
        blocksByUri.put(block.getURI(), block);
        idByUri.put(block.getURI(), block.getId());
    }

    /**
     * Retrieve all {@code BlockUri}s that match the given string.
     * <p/>
     * In order to resolve the {@code BlockUri}s, every package is searched for the given uri pattern.
     *
     * @param uri the uri pattern to match
     * @return a list of matching block uris
     */
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

    public Map<String, Byte> getBlockIdMap() {
        Map<String, Byte> result = Maps.newHashMapWithExpectedSize(idByUri.size());
        TObjectByteIterator<BlockUri> iterator = idByUri.iterator();
        while (iterator.hasNext()) {
            iterator.advance();
            result.put(iterator.key().toString(), iterator.value());
        }
        return result;
    }

    public Iterable<BlockUri> getBlockFamiliesWithCategory(String category) {
        return categoryLookup.get(category.toLowerCase(Locale.ENGLISH));
    }

    public Iterable<String> getBlockCategories() {
        return categoryLookup.keySet();
    }

    public BlockFamily getBlockFamily(String uri) {
        return getBlockFamily(new BlockUri(uri));
    }

    public BlockFamily getBlockFamily(BlockUri uri) {
        return registeredFamilyByUri.get(uri);
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
        if (result == null) {
            return blocksById.get((byte) 0);
        }
        return result;
    }

    public Iterable<BlockUri> listRegisteredBlockUris() {
        return registeredFamilyByUri.keySet();
    }

    public Iterable<BlockFamily> listRegisteredBlockFamilies() {
        return registeredFamilyByUri.values();
    }

    public int registeredBlockFamiliesCount() {
        return registeredFamilyByUri.size();
    }

    public Iterable<BlockUri> listFreeformBlockUris() {
        return freeformBlockUris;
    }

    public boolean isFreeformFamily(BlockUri familyUri) {
        return freeformBlockUris.contains(familyUri);
    }

    public Iterable<BlockFamily> listAvailableBlockFamilies() {
        return availableFamilies.values();
    }

    public BlockFamily getAvailableBlockFamily(BlockUri uri) {
        return availableFamilies.get(uri);
    }

    public Iterable<BlockUri> listAvailableBlockUris() {
        return availableFamilies.keySet();
    }

    public int getBlockFamilyCount() {
        return registeredFamilyByUri.size();
    }

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
        return registeredFamilyByUri.containsKey(uri) || availableFamilies.containsKey(uri) || freeformBlockUris.contains(uri);
    }
}

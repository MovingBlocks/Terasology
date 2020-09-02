/*
 * Copyright 2018 MovingBlocks
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
package org.terasology.world.block.internal;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import gnu.trove.iterator.TObjectShortIterator;
import gnu.trove.map.TObjectShortMap;
import gnu.trove.map.TShortObjectMap;
import gnu.trove.map.hash.TObjectShortHashMap;
import gnu.trove.map.hash.TShortObjectHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.ResourceUrn;
import org.terasology.assets.management.AssetManager;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.BlockUriParseException;
import org.terasology.world.block.family.BlockFamily;
import org.terasology.world.block.loader.BlockFamilyDefinition;
import org.terasology.world.block.shapes.BlockShape;
import org.terasology.world.block.tiles.WorldAtlas;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

public class BlockManagerImpl extends BlockManager {

    private static final Logger logger = LoggerFactory.getLogger(BlockManagerImpl.class);

    // This is the id we assign to blocks whose mappings are missing. This shouldn't happen, but in case it does
    // we set them to the last id (don't want to use 0 as they would override air)
    private static final short UNKNOWN_ID = (short) 65535;
    private static final int MAX_ID = 65534;
    private static final ResourceUrn CUBE_SHAPE_URN = new ResourceUrn("engine:cube");

    private AssetManager assetManager;

    private BlockBuilder blockBuilder;

    private ReentrantLock lock = new ReentrantLock();

    private AtomicReference<RegisteredState> registeredBlockInfo = new AtomicReference<>(new RegisteredState());


    private boolean generateNewIds;
    private AtomicInteger nextId = new AtomicInteger(1);

    // Cache this for performance reasons because a lookup by BlockURI happens the first time a block is set when getting the previous block.
    // This causes performance problems eventually down the line when it then uses the ResourceUrn's hashcode to do a lookup into the block map.
    private Block airBlock;

    public BlockManagerImpl(WorldAtlas atlas, AssetManager assetManager) {
        this(atlas, assetManager, true);
    }

    public BlockManagerImpl(WorldAtlas atlas,
                            AssetManager assetManager,
                            boolean generateNewIds) {
        this.generateNewIds = generateNewIds;
        this.assetManager = assetManager;
        this.blockBuilder = new BlockBuilder(atlas);
    }

    /**
     * Put all the existing families and blocks from the manifest back with the
     * same IDs, and if this is the authority (singleplayer or server), check
     * for any other block family definitions and register all of those as well.
     */
    public void initialise(List<String> registeredBlockFamilies,
                           Map<String, Short> knownBlockMappings) {

        if (knownBlockMappings.size() >= MAX_ID) {
            nextId.set(UNKNOWN_ID);
        } else if (knownBlockMappings.size() > 0) {
            nextId.set(knownBlockMappings.values().stream().max(Short::compareTo).orElse((short) 0) + 1);
        }
        registeredBlockInfo.set(new RegisteredState());

        for (String rawFamilyUri : registeredBlockFamilies) {
            try {
                addFamily(new BlockUri(rawFamilyUri), knownBlockMappings);
            } catch (BlockUriParseException e) {
                logger.error("Failed to parse block family, skipping", e);
            }
        }
        if (generateNewIds) {
            Set<ResourceUrn> availableFamilies = assetManager.getAvailableAssets(BlockFamilyDefinition.class);
            for (ResourceUrn familyUrn : availableFamilies) {
                BlockFamily existingFamily = getBlockFamily(new BlockUri(familyUrn));
                if (null == existingFamily) {
                    assetManager.getAsset(familyUrn, BlockFamilyDefinition.class).ifPresent(def -> {
                        if (def.isFreeform()) {
                            for (BlockShape shape : def.getData().getBaseSection().getShapes()) {
                                addFamily(new BlockUri(familyUrn, shape.getUrn()), knownBlockMappings);
                            }
                        } else {
                            addFamily(new BlockUri(familyUrn), knownBlockMappings);
                        }
                    });
                }
            }
        }
    }
    
    /**
     * Load a family from its assets and then register it and its blocks, respecting existing block IDs.
     */
    private void addFamily(BlockUri familyUri, Map<String, Short> knownBlockMappings) {
        Optional<BlockFamily> family = loadFamily(familyUri);
        if (family.isPresent()) {
            for (Block block : family.get().getBlocks()) {
                Short id = knownBlockMappings.get(block.getURI().toString());
                if (id != null) {
                    block.setId(id);
                } else {
                    logger.error("Missing id for block {} in provided family {}", block.getURI(), family.get().getURI());
                    if (generateNewIds) {
                        block.setId(getNextId());
                    } else {
                        block.setId(UNKNOWN_ID);
                    }
                }
            }
            registerFamily(family.get());
        }
    }

    public void dispose() {

    }

    private short getNextId() {
        if (nextId.get() > MAX_ID) {
            return UNKNOWN_ID;
        }
        return (short) nextId.getAndIncrement();
    }

    /**
     * Get cached instance of the air block.
     *
     * We do this for performance reasons because a lookup by BlockURI happens the first time a block is set when
     * getting the previous block. This causes performance problems eventually down the line when it then uses the
     * ResourceUrn's hashcode to do a lookup into the block map.
     */
    private Block getAirBlock() {
        if (airBlock == null) {
            airBlock = getBlock(AIR_ID);
        }
        return airBlock;
    }

    /**
     * Add an already generated family and all its contained blocks to the lists.
     */
    @VisibleForTesting
    protected void registerFamily(BlockFamily family) {
        Preconditions.checkNotNull(family);
        logger.info("Registered {}", family);
        lock.lock();
        try {
            RegisteredState newState = new RegisteredState(registeredBlockInfo.get());
            newState.registeredFamilyByUri.put(family.getURI(), family);
            for (Block block : family.getBlocks()) {
                registerBlock(block, newState);
            }
            registeredBlockInfo.set(newState);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Add an already generated block to the lists.
     */
    private void registerBlock(Block block, RegisteredState newState) {
        if (block.getId() != UNKNOWN_ID) {
            logger.info("Registered Block {} with id {}", block, block.getId());
            newState.blocksById.put(block.getId(), block);
            newState.idByUri.put(block.getURI(), block.getId());
        } else {
            logger.info("Failed to register block {} - no id", block);
        }
        newState.blocksByUri.put(block.getURI(), block);
    }

    @Override
    public Map<String, Short> getBlockIdMap() {
        Map<String, Short> result = Maps.newHashMapWithExpectedSize(registeredBlockInfo.get().idByUri.size());
        TObjectShortIterator<BlockUri> iterator = registeredBlockInfo.get().idByUri.iterator();
        while (iterator.hasNext()) {
            iterator.advance();
            result.put(iterator.key().toString(), iterator.value());
        }
        return result;
    }

    /**
     * Recall the family registered with the given name.
     */
    @Override
    public BlockFamily getBlockFamily(String uri) {
        if (!uri.contains(":")) {
            Set<ResourceUrn> resourceUrns = assetManager.resolve(uri, BlockFamilyDefinition.class);
            if (resourceUrns.size() == 1) {
                return getBlockFamily(new BlockUri(resourceUrns.iterator().next()));
            } else {
                if (resourceUrns.size() > 0) {
                    logger.error("Failed to resolve block family '{}', too many options - {}", uri, resourceUrns);
                } else {
                    logger.error("Failed to resolve block family '{}'", uri);
                }
            }
        } else {
            try {
                BlockUri blockUri = new BlockUri(uri);
                return getBlockFamily(blockUri);
            } catch (BlockUriParseException e) {
                logger.error("Failed to resolve block family '{}', invalid uri", uri);
            }
        }
        return getBlockFamily(AIR_ID);
    }

    /**
     * Recall the family registered with the given name.
     */
    @Override
    public BlockFamily getBlockFamily(BlockUri uri) {
        if (uri.getShapeUrn().isPresent() && uri.getShapeUrn().get().equals(CUBE_SHAPE_URN)) {
            return getBlockFamily(uri.getShapelessUri());
        }
        return registeredBlockInfo.get().registeredFamilyByUri.get(uri);
    }

    /**
     * Create a block family by loading the relevant assets.
     */
    private Optional<BlockFamily> loadFamily(BlockUri uri) {
        Optional<BlockFamilyDefinition> familyDef = assetManager.getAsset(uri.getBlockFamilyDefinitionUrn(), BlockFamilyDefinition.class);
        if (familyDef.isPresent() && familyDef.get().isLoadable()) {
            if (familyDef.get().isFreeform()) {
                ResourceUrn shapeUrn;
                if (uri.getShapeUrn().isPresent()) {
                    shapeUrn = uri.getShapeUrn().get();
                } else {
                    shapeUrn = CUBE_SHAPE_URN;
                }
                Optional<BlockShape> shape = assetManager.getAsset(shapeUrn, BlockShape.class);
                if (shape.isPresent()) {
                    return Optional.of(familyDef.get().createFamily(shape.get(), blockBuilder));
                }
            } else {
                return Optional.of(familyDef.get().createFamily(blockBuilder));
            }
        } else {
            logger.error("Family not available: {}", uri);
        }
        return Optional.empty();
    }

    /**
     * Recall the block registered with the given name.
     */
    @Override
    public Block getBlock(String uri) {
        try {
            return getBlock(new BlockUri(uri));
        } catch (BlockUriParseException e) {
            logger.error("Attempt to fetch block with illegal uri '{}'", uri);
            return getAirBlock();
        }
    }

    /**
     * Recall the block registered with the given name.
     */
    @Override
    public Block getBlock(BlockUri uri) {
        if (uri.getShapeUrn().isPresent() && uri.getShapeUrn().get().equals(CUBE_SHAPE_URN)) {
            return getBlock(uri.getShapelessUri());
        }
        Block block = registeredBlockInfo.get().blocksByUri.get(uri);
        if (block == null) {
            throw new NullPointerException("Attempt to fetch block with unknown uri '" + uri + "'");
        } else {
            return block;
        }
    }

    /**
     * Recall the block registered with the given id.
     */
    @Override
    public Block getBlock(short id) {
        Block result = registeredBlockInfo.get().blocksById.get(id);
        if (result == null) {
            return getAirBlock();
        }
        return result;
    }

    @Override
    public Collection<BlockUri> listRegisteredBlockUris() {
        return Collections.unmodifiableCollection(registeredBlockInfo.get().registeredFamilyByUri.keySet());
    }

    @Override
    public Collection<BlockFamily> listRegisteredBlockFamilies() {
        return Collections.unmodifiableCollection(registeredBlockInfo.get().registeredFamilyByUri.values());
    }

    @Override
    public int getBlockFamilyCount() {
        return registeredBlockInfo.get().registeredFamilyByUri.size();
    }

    @Override
    public Collection<Block> listRegisteredBlocks() {
        return ImmutableList.copyOf(registeredBlockInfo.get().blocksById.valueCollection());
    }

    private static class RegisteredState {
        private final Map<BlockUri, BlockFamily> registeredFamilyByUri;

        /* Blocks */
        private final Map<BlockUri, Block> blocksByUri;
        private final TShortObjectMap<Block> blocksById;
        private final TObjectShortMap<BlockUri> idByUri;

        RegisteredState() {
            this.registeredFamilyByUri = Maps.newHashMap();
            this.blocksByUri = Maps.newHashMap();
            this.blocksById = new TShortObjectHashMap<>();
            this.idByUri = new TObjectShortHashMap<>();
        }

        RegisteredState(RegisteredState oldState) {
            this.registeredFamilyByUri = Maps.newHashMap(oldState.registeredFamilyByUri);
            this.blocksByUri = Maps.newHashMap(oldState.blocksByUri);
            this.blocksById = new TShortObjectHashMap<>(oldState.blocksById);
            this.idByUri = new TObjectShortHashMap<>(oldState.idByUri);
        }
    }
}

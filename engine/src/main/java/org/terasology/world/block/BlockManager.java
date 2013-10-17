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
package org.terasology.world.block;

import org.terasology.engine.TerasologyConstants;
import org.terasology.world.block.family.BlockFamily;
import org.terasology.world.block.family.SymmetricFamily;

import java.util.List;
import java.util.Map;

/**
 * @author Immortius
 */
public abstract class BlockManager {

    private static final String AIR_ID = "air";
    private static final Block AIR;
    private static final BlockFamily AIR_FAMILY;

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
        AIR.setId((short) 0);
        AIR.setDisplayName("Air");
        AIR.setUri(new BlockUri(TerasologyConstants.ENGINE_MODULE, AIR_ID));
        AIR_FAMILY = new SymmetricFamily(AIR.getURI(), AIR);
    }

    public static Block getAir() {
        return AIR;
    }

    public static BlockFamily getAirFamily() {
        return AIR_FAMILY;
    }

    /**
     * Retrieve all {@code BlockUri}s that match the given string.
     * <p/>
     * In order to resolve the {@code BlockUri}s, every package is searched for the given uri pattern.
     *
     * @param uri the uri pattern to match
     * @return a list of matching block uris
     */
    public abstract List<BlockUri> resolveBlockUri(String uri);

    /**
     * @return A map of the mapping between Block Uris and Ids
     */
    public abstract Map<String, Short> getBlockIdMap();

    /**
     * @param category
     * @return Iterable over all block families with the given category
     */
    public abstract Iterable<BlockUri> getBlockFamiliesWithCategory(String category);

    /**
     * @return Iterable over all block categories
     */
    public abstract Iterable<String> getBlockCategories();

    /**
     * @param uri
     * @return Retrieves the BlockFamily for the given uri, or null if there isn't one
     */
    public abstract BlockFamily getBlockFamily(String uri);

    /**
     * @param uri
     * @return Retrieves the BlockFamily for the given uri, or null if there isn't one
     */
    public abstract BlockFamily getBlockFamily(BlockUri uri);

    /**
     * @param uri
     * @return Retrieves the Block for the given uri, or null if there isn't one
     */
    public abstract Block getBlock(String uri);

    /**
     * @param uri
     * @return Retrieves the Block for the given uri, or null if there isn't one
     */
    public abstract Block getBlock(BlockUri uri);

    /**
     * @param id
     * @return Retrieves the Block with the given id, or null if there isn't one
     */
    public abstract Block getBlock(short id);

    /**
     * @return An iterable over the list of registered (in use) block uris
     */
    public abstract Iterable<BlockUri> listRegisteredBlockUris();

    /**
     * @return An iterable over the list of registered (in use) block families
     */
    public abstract Iterable<BlockFamily> listRegisteredBlockFamilies();

    /**
     * @return A list of freeform block uris
     */
    public abstract Iterable<BlockUri> listFreeformBlockUris();

    /**
     * @param familyUri
     * @return Whether the given block family is freeform (can be created with any shape)
     */
    public abstract boolean isFreeformFamily(BlockUri familyUri);

    /**
     * @return A list of available block families
     */
    public abstract Iterable<BlockFamily> listAvailableBlockFamilies();

    /**
     * Retrieves a block family without registering
     *
     * @param uri
     * @return The block family with the given uri, if available
     */
    public abstract BlockFamily getAvailableBlockFamily(BlockUri uri);

    /**
     * @return A list of the block uris of available blocks.
     */
    public abstract Iterable<BlockUri> listAvailableBlockUris();

    /**
     * @return The number of block families
     */
    public abstract int getBlockFamilyCount();

    /**
     * @param uri
     * @return Whether the uri corresponds to a block family.
     */
    public abstract boolean hasBlockFamily(BlockUri uri);

    public abstract Iterable<Block> listRegisteredBlocks();

}

// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.block;

import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.engine.world.block.family.BlockFamily;

import java.util.Collection;
import java.util.Map;

/**
 */
public abstract class BlockManager {

    public static final BlockUri AIR_ID = new BlockUri(new ResourceUrn("engine:air"));
    public static final BlockUri UNLOADED_ID = new BlockUri(new ResourceUrn("engine:unloaded"));

    /**
     * @return A map of the mapping between Block Uris and Ids
     */
    public abstract Map<String, Short> getBlockIdMap();

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
     * @return A collection of registered (in use) block uris
     */
    public abstract Collection<BlockUri> listRegisteredBlockUris();

    /**
     * @return A collection of registered (in use) block families
     */
    public abstract Collection<BlockFamily> listRegisteredBlockFamilies();

    /**
     * @return The count of registered block families
     */
    public abstract int getBlockFamilyCount();

    /**
     * @return A collection of registered blocks
     */
    public abstract Collection<Block> listRegisteredBlocks();

}

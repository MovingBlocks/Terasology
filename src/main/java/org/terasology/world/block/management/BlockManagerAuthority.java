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

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.family.BlockFamily;
import org.terasology.world.block.loader.BlockLoader;
import org.terasology.world.block.loader.FreeformFamily;

import java.util.Map;

/**
 * Authorative BlockManager, assigns ids to and registers blocks as their block families are requested.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class BlockManagerAuthority extends BlockManager {

    private static final Logger logger = LoggerFactory.getLogger(BlockManagerAuthority.class);

    private int nextId = 1;
    private BlockLoader blockLoader;

    public BlockManagerAuthority() {
        this(Maps.<String, Byte>newHashMap());
    }

    public BlockManagerAuthority(Map<String, Byte> knownBlockMappings) {
        blockLoader = new BlockLoader();
        BlockLoader.LoadBlockDefinitionResults blockDefinitions = blockLoader.loadBlockDefinitions();
        addBlockFamily(getAirFamily(), true);
        for (BlockFamily family : blockDefinitions.families) {
            addBlockFamily(family, false);
        }
        for (FreeformFamily freeformFamily : blockDefinitions.shapelessDefinitions) {
            addFreeformBlockFamily(freeformFamily.uri, freeformFamily.categories);
        }
        blockLoader.buildAtlas();
        nextId = knownBlockMappings.size();
        if (nextId == 0) {
            // Account for air being registered.
            nextId++;
        }

        for (String uri : knownBlockMappings.keySet()) {
            BlockUri blockUri = new BlockUri(uri);
            BlockUri familyUri = blockUri.getRootFamilyUri();
            BlockFamily family;
            if (isFreeformFamily(familyUri)) {
                family = blockLoader.loadWithShape(blockUri.getFamilyUri());
            } else {
                family = getAvailableBlockFamily(familyUri);
            }
            if (family != null) {
                for (Block block : family.getBlocks()) {
                    Byte id = knownBlockMappings.get(block.getURI().toString());
                    if (id != null) {
                        block.setId(id);
                    } else {
                        logger.warn("Missing id for block {} in previously used family {}", block.getURI(), family.getURI());
                        block.setId((byte) (nextId++));
                    }
                }
                registerFamily(family);
            } else {
                logger.warn("Block no longer available: {}", uri);
            }
        }
    }

    @Override
    public BlockFamily getBlockFamily(BlockUri uri) {
        BlockFamily family = super.getBlockFamily(uri);
        if (family == null) {
            if (isFreeformFamily(uri.getRootFamilyUri())) {
                family = blockLoader.loadWithShape(uri);
            } else {
                family = getAvailableBlockFamily(uri);
            }
            if (family != null) {
                for (Block block : family.getBlocks()) {
                    block.setId((byte) nextId++);
                }
                registerFamily(family);
            }
        }
        return family;
    }

}

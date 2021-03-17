// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.persistence.typeHandling.extensionTypes;

import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.block.family.BlockFamily;
import org.terasology.persistence.typeHandling.StringRepresentationTypeHandler;

/**
 * Return the block family based on the registered string id.
 */
public class BlockFamilyTypeHandler extends StringRepresentationTypeHandler<BlockFamily> {

    private BlockManager blockManager;

    public BlockFamilyTypeHandler(BlockManager blockManager) {
        this.blockManager = blockManager;
    }

    @Override
    public String getAsString(BlockFamily item) {
        if (item == null) {
            return "";
        }
        return item.getURI().toString();
    }

    @Override
    public BlockFamily getFromString(String representation) {
        return blockManager.getBlockFamily(representation);
    }

}

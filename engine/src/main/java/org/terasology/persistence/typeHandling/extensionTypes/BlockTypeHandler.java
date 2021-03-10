// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.persistence.typeHandling.extensionTypes;

import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.persistence.typeHandling.StringRepresentationTypeHandler;

/**
 */
public class BlockTypeHandler extends StringRepresentationTypeHandler<Block> {

    private BlockManager blockManager;

    public BlockTypeHandler(BlockManager blockManager) {
        this.blockManager = blockManager;
    }

    @Override
    public String getAsString(Block item) {
        if (item == null) {
            return "";
        }
        return item.getURI().toString();
    }

    @Override
    public Block getFromString(String representation) {
        return blockManager.getBlock(representation);
    }

}

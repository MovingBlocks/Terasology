// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.logic.console.commandSystem.adapter;

import com.google.common.base.Preconditions;
import org.terasology.registry.In;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.family.BlockFamily;

/**
 *
 */
public class BlockFamilyAdapter implements ParameterAdapter<BlockFamily> {
    @In
    BlockManager blockManager;

    @Override
    public BlockFamily parse(String raw) {
        Preconditions.checkNotNull(raw, "'raw' must not be null!");
        return blockManager.getBlockFamily(raw);
    }

    @Override
    public String convertToString(BlockFamily value) {
        Preconditions.checkNotNull(value, "'value' must not be null!");
        return value.getURI().toString();
    }
}

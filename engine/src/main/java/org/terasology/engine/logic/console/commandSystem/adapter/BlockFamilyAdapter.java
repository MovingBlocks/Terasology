// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.console.commandSystem.adapter;

import com.google.common.base.Preconditions;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.block.family.BlockFamily;

public class BlockFamilyAdapter implements ParameterAdapter<BlockFamily> {
    @Override
    public BlockFamily parse(String raw) {
        Preconditions.checkNotNull(raw, "'raw' must not be null!");
        return CoreRegistry.get(BlockManager.class).getBlockFamily(raw);
    }

    @Override
    public String convertToString(BlockFamily value) {
        Preconditions.checkNotNull(value, "'value' must not be null!");
        return value.getURI().toString();
    }
}

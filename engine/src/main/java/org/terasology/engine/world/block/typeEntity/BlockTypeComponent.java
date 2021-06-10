// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.block.typeEntity;

import org.terasology.engine.network.Replicate;
import org.terasology.engine.world.block.Block;
import org.terasology.gestalt.entitysystem.component.Component;

public class BlockTypeComponent implements Component<BlockTypeComponent> {
    @Replicate
    public Block block;

    @Override
    public void copy(BlockTypeComponent other) {
        this.block = other.block;
    }
}

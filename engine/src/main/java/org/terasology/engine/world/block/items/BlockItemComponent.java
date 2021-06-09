// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.block.items;

import org.terasology.engine.network.FieldReplicateType;
import org.terasology.engine.network.Replicate;
import org.terasology.engine.world.block.family.BlockFamily;
import org.terasology.gestalt.entitysystem.component.Component;

/**
 * Combined with ItemComponent, represents a held block
 *
 */
public final class BlockItemComponent implements Component<BlockItemComponent> {
    @Replicate(FieldReplicateType.SERVER_TO_OWNER)
    public BlockFamily blockFamily;

    public BlockItemComponent() {
    }

    public BlockItemComponent(BlockFamily blockFamily) {
        this.blockFamily = blockFamily;
    }

    @Override
    public void copy(BlockItemComponent other) {
        this.blockFamily = other.blockFamily;
    }
}

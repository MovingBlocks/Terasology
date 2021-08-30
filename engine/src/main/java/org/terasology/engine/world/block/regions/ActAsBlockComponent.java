// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.block.regions;


import org.terasology.engine.world.block.family.BlockFamily;
import org.terasology.gestalt.entitysystem.component.Component;

/**
 * An entity with this component will act as that block - producing block style damage effects, take damage as that block would.
 *
 */
public final class ActAsBlockComponent implements Component<ActAsBlockComponent> {
    public BlockFamily block;
    public boolean dropBlocksInRegion;

    @Override
    public void copyFrom(ActAsBlockComponent other) {
        this.block = other.block;
        this.dropBlocksInRegion = other.dropBlocksInRegion;
    }
}

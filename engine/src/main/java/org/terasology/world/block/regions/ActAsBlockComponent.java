// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.block.regions;


import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.world.block.family.BlockFamily;

/**
 * An entity with this component will act as that block - producing block style damage effects, take damage as that block would.
 *
 */
public final class ActAsBlockComponent implements Component {
    public BlockFamily block;
    public boolean dropBlocksInRegion;
}

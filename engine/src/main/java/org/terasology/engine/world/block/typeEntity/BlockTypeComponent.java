// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.block.typeEntity;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.network.Replicate;
import org.terasology.engine.world.block.Block;

public class BlockTypeComponent implements Component {
    @Replicate
    public Block block;
}

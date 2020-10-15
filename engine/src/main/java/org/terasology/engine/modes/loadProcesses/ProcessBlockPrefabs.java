// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.modes.loadProcesses;

import org.terasology.engine.modes.ExpectedCost;
import org.terasology.engine.modes.SingleStepLoadProcess;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.registry.In;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.internal.BlockManagerImpl;
import org.terasology.world.block.internal.BlockPrefabManager;


@ExpectedCost(1)
public class ProcessBlockPrefabs extends SingleStepLoadProcess {

    @In
    BlockManager blockManager;
    @In
    EntityManager entityManager;

    @Override
    public String getMessage() {
        return "Initialising Block Type Entities";
    }

    @Override
    public boolean step() {
        BlockManagerImpl blockManagerImpl = (BlockManagerImpl) blockManager;
        blockManagerImpl.subscribe(new BlockPrefabManager(entityManager, blockManagerImpl));
        return true;
    }
}

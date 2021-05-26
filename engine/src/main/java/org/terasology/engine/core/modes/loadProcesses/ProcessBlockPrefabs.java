// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core.modes.loadProcesses;

import org.terasology.engine.context.Context;
import org.terasology.engine.core.modes.SingleStepLoadProcess;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.block.internal.BlockManagerImpl;
import org.terasology.engine.world.block.internal.BlockPrefabManager;

public class ProcessBlockPrefabs extends SingleStepLoadProcess {

    private final Context context;

    public ProcessBlockPrefabs(Context context) {
        this.context = context;
    }

    @Override
    public String getMessage() {
        return "Initialising Block Type Entities";
    }

    @Override
    public boolean step() {
        BlockManagerImpl blockManager = (BlockManagerImpl) context.get(BlockManager.class);
        blockManager.subscribe(new BlockPrefabManager(context.get(EntityManager.class), blockManager));
        return true;
    }

    @Override
    public int getExpectedCost() {
        return 1;
    }
}

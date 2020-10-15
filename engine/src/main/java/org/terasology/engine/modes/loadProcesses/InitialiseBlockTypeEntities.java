// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.modes.loadProcesses;

import org.terasology.engine.modes.ExpectedCost;
import org.terasology.engine.modes.SingleStepLoadProcess;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.registry.In;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.internal.BlockManagerImpl;
import org.terasology.world.block.typeEntity.BlockTypeEntityGenerator;

@ExpectedCost(1)
public class InitialiseBlockTypeEntities extends SingleStepLoadProcess {

    @In
    private BlockManager blockManager;
    @In
    private EntityManager entityManager;

    @Override
    public String getMessage() {
        return "Initialising Block Type Entities";
    }

    @Override
    public boolean step() {
        ((BlockManagerImpl) blockManager).subscribe(new BlockTypeEntityGenerator(entityManager, blockManager));
        return true;
    }
}

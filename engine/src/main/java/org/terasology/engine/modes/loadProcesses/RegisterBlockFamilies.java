// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.modes.loadProcesses;

import org.terasology.context.Context;
import org.terasology.engine.modes.ExpectedCost;
import org.terasology.engine.modes.SingleStepLoadProcess;
import org.terasology.registry.In;
import org.terasology.registry.InjectionHelper;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.family.BlockFamily;

@ExpectedCost(1)
public class RegisterBlockFamilies extends SingleStepLoadProcess {

    @In
    private Context context;
    @In
    private BlockManager blockManager;

    @Override
    public String getMessage() {
        return "Registering Block Families ...";
    }

    @Override
    public boolean step() {
        for (BlockFamily blockFamily : blockManager.listRegisteredBlockFamilies()) {
            InjectionHelper.inject(blockFamily, context);
        }
        return  true;
    }
}

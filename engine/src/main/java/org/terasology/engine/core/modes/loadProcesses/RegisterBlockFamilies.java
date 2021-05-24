// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core.modes.loadProcesses;

import org.terasology.engine.context.Context;
import org.terasology.engine.core.modes.SingleStepLoadProcess;
import org.terasology.engine.registry.InjectionHelper;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.block.family.BlockFamily;

public class RegisterBlockFamilies  extends SingleStepLoadProcess {
    private final Context context;

    public RegisterBlockFamilies(Context context) {
        this.context = context;
    }

    @Override
    public String getMessage() {
        return "Registering Block Families ...";
    }

    @Override
    public boolean step() {
        BlockManager blockManager = context.get(BlockManager.class);
        for (BlockFamily blockFamily : blockManager.listRegisteredBlockFamilies()) {
            InjectionHelper.inject(blockFamily, context);
        }
        return  true;
    }

    @Override
    public int getExpectedCost() {
        return 1;
    }
}

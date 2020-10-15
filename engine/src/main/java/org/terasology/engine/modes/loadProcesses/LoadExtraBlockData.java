// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.modes.loadProcesses;

import org.terasology.engine.modes.ExpectedCost;
import org.terasology.engine.modes.SingleStepLoadProcess;
import org.terasology.registry.ContextAwareClassFactory;
import org.terasology.registry.In;
import org.terasology.world.chunks.blockdata.ExtraBlockDataManager;

/**
 * Sets up an ExtraBlockDataManager based on @ExtraBlockSystem classes from the loaded modules. Depends on block
 * definitions and module classes already being loaded.
 */
@ExpectedCost(1)
public class LoadExtraBlockData extends SingleStepLoadProcess {
    @In
    private ContextAwareClassFactory classFactory;

    @Override
    public String getMessage() {
        return "Loading extra block data fields...";
    }

    @Override
    public boolean step() {
        classFactory.createInjectableInstance(ExtraBlockDataManager.class);
        return true;
    }
}

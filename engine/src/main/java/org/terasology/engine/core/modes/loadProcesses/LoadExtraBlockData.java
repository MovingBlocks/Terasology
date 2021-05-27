// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.core.modes.loadProcesses;

import org.terasology.engine.context.Context;
import org.terasology.engine.core.modes.SingleStepLoadProcess;
import org.terasology.engine.world.chunks.blockdata.ExtraBlockDataManager;

/**
 * Sets up an ExtraBlockDataManager based on @ExtraBlockSystem classes from the loaded modules.
 * Depends on block definitions and module classes already being loaded.
 */
public class LoadExtraBlockData extends SingleStepLoadProcess {
    private final Context context;
    
    public LoadExtraBlockData(Context context) {
        this.context = context;
    }

    @Override
    public String getMessage() {
        return "Loading extra block data fields...";
    }
    
    @Override
    public boolean step() {
        context.put(ExtraBlockDataManager.class, new ExtraBlockDataManager(context));
        return true;
    }
    
    @Override
    public int getExpectedCost() {
        return 1;
    }
}

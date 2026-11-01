// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.core.modes.loadProcesses;

import org.terasology.context.Lifetime;
import org.terasology.engine.core.modes.SingleStepLoadProcess;
import org.terasology.engine.world.chunks.blockdata.ExtraBlockDataManager;
import org.terasology.gestalt.di.ServiceRegistry;

/**
 * Sets up an ExtraBlockDataManager based on @ExtraBlockSystem classes from the loaded modules.
 * Depends on block definitions and module classes already being loaded.
 */
public class LoadExtraBlockData extends SingleStepLoadProcess {
    private final ServiceRegistry serviceRegistry;
    
    public LoadExtraBlockData(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    @Override
    public String getMessage() {
        return "Loading extra block data fields...";
    }
    
    @Override
    public boolean step() {
        serviceRegistry.with(ExtraBlockDataManager.class).lifetime(Lifetime.Singleton).use(ExtraBlockDataManager.class);
        return true;
    }
    
    @Override
    public int getExpectedCost() {
        return 1;
    }
}

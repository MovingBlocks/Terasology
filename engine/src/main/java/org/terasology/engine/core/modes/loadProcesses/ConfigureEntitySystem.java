// Copyright 2026 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.core.modes.loadProcesses;

import org.terasology.engine.context.Context;
import org.terasology.engine.core.bootstrap.EntitySystemSetupUtil;
import org.terasology.engine.core.modes.SingleStepLoadProcess;
import org.terasology.engine.core.module.ModuleManager;
import org.terasology.engine.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.engine.entitySystem.metadata.EntitySystemLibrary;
import org.terasology.persistence.typeHandling.TypeHandlerLibrary;

public class ConfigureEntitySystem extends SingleStepLoadProcess {

    private final Context context;

    public ConfigureEntitySystem(Context context) {
        this.context = context;
    }

    @Override
    public String getMessage() {
        return "Configuring Entity System...";
    }

    @Override
    public boolean step() {
        EntitySystemSetupUtil.configureEntityManagementRelatedClasses(
                context.get(TypeHandlerLibrary.class),
                context.get(EntitySystemLibrary.class),
                context.get(ModuleManager.class).getEnvironment(),
                context.get(EngineEntityManager.class)
        );
        return true;
    }

    @Override
    public int getExpectedCost() {
        return 1;
    }
}

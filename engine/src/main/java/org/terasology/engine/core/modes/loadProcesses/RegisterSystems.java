// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.core.modes.loadProcesses;

import org.terasology.engine.context.Context;
import org.terasology.engine.core.ComponentSystemManager;
import org.terasology.engine.core.GameEngine;
import org.terasology.engine.core.TerasologyEngine;
import org.terasology.engine.core.modes.SingleStepLoadProcess;
import org.terasology.engine.core.module.ModuleManager;
import org.terasology.engine.core.subsystem.EngineSubsystem;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.internal.PojoEntityManager;
import org.terasology.engine.entitySystem.prefab.PrefabManager;
import org.terasology.engine.network.NetworkMode;

public class RegisterSystems extends SingleStepLoadProcess {
    private final Context context;
    private final NetworkMode netMode;
    private ComponentSystemManager componentSystemManager;

    public RegisterSystems(Context context, NetworkMode netMode) {
        this.context = context;
        this.netMode = netMode;
    }

    @Override
    public String getMessage() {
        return "Registering systems...";
    }

    @Override
    public boolean step() {
        componentSystemManager = context.get(ComponentSystemManager.class);
        ModuleManager moduleManager = context.get(ModuleManager.class);

        TerasologyEngine terasologyEngine = (TerasologyEngine) context.get(GameEngine.class);
        for (EngineSubsystem subsystem : terasologyEngine.getSubsystems()) {
            subsystem.registerSystems(componentSystemManager);
        }
        componentSystemManager.loadSystems(moduleManager.getEnvironment(), netMode);

        ((PojoEntityManager) context.get(EntityManager.class)).setPrefabManager(context.get(PrefabManager.class));

        return true;
    }


    @Override
    public int getExpectedCost() {
        return 1;
    }

}

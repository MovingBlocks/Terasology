// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.core.modes.loadProcesses;

import org.terasology.engine.context.Context;
import org.terasology.engine.core.ComponentSystemManager;
import org.terasology.engine.core.modes.SingleStepLoadProcess;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.engine.entitySystem.metadata.EventLibrary;
import org.terasology.engine.network.NetworkSystem;
import org.terasology.engine.world.BlockEntityRegistry;

public class InitialiseSystems extends SingleStepLoadProcess {

    private final Context context;

    public InitialiseSystems(Context context) {
        this.context = context;
    }

    @Override
    public String getMessage() {
        return "Initialising Systems...";
    }

    @Override
    public boolean step() {
        EngineEntityManager entityManager = (EngineEntityManager) context.get(EntityManager.class);
        EventLibrary eventLibrary = context.get(EventLibrary.class);
        BlockEntityRegistry blockEntityRegistry = context.get(BlockEntityRegistry.class);

        context.get(NetworkSystem.class).connectToEntitySystem(entityManager, eventLibrary, blockEntityRegistry);
        ComponentSystemManager csm = context.get(ComponentSystemManager.class);
        csm.initialise();

        return true;
    }

    @Override
    public int getExpectedCost() {
        return 1;
    }

}

// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.modes.loadProcesses;

import org.terasology.engine.ComponentSystemManager;
import org.terasology.engine.modes.ExpectedCost;
import org.terasology.engine.modes.SingleStepLoadProcess;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.entitySystem.metadata.EventLibrary;
import org.terasology.network.NetworkSystem;
import org.terasology.registry.In;
import org.terasology.world.BlockEntityRegistry;

@ExpectedCost(1)
public class InitialiseSystems extends SingleStepLoadProcess {

    @In
    private ComponentSystemManager csm;
    @In
    private NetworkSystem networkSystem;
    @In
    private EventLibrary eventLibrary;
    @In
    private EntityManager entityManager;
    @In
    private BlockEntityRegistry blockEntityRegistry;

    @Override
    public String getMessage() {
        return "Initialising Systems...";
    }

    @Override
    public boolean step() {
        networkSystem.connectToEntitySystem((EngineEntityManager) entityManager, eventLibrary, blockEntityRegistry);
        csm.initialise();
        return true;
    }
}

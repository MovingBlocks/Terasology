// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.modes.loadProcesses;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.modes.ExpectedCost;
import org.terasology.engine.modes.SingleStepLoadProcess;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.persistence.StorageManager;
import org.terasology.registry.In;

import java.io.IOException;

@ExpectedCost(1)
public class LoadEntities extends SingleStepLoadProcess {

    private static final Logger logger = LoggerFactory.getLogger(LoadEntities.class);

    @In
    private EntityManager entityManager;
    @In
    private StorageManager storageManager;

    @Override
    public String getMessage() {
        return "Loading Entities";
    }

    @Override
    public boolean step() {
        boolean entityCreated = false;
        for (EntityRef entity : entityManager.getAllEntities()) {
            entityCreated = true;
            logger.error("Entity created before load: {}", entity.toFullDescription());
        }
        if (entityCreated) {
            throw new IllegalStateException("Entity creation detected during component system initialisation, game load aborting");
        }
        try {
            storageManager.loadGlobalStore();
        } catch (IOException e) {
            logger.error("Failed to load global data.", e);
        }
        return true;
    }
}

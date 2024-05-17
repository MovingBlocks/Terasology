// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.core.modes.loadProcesses;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.modes.SingleStepLoadProcess;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.persistence.StorageManager;

import java.io.IOException;

public class LoadEntities extends SingleStepLoadProcess {

    private static final Logger logger = LoggerFactory.getLogger(LoadEntities.class);

    private final Context context;

    public LoadEntities(Context context) {
        this.context = context;
    }

    @Override
    public String getMessage() {
        return "Loading Entities";
    }

    @Override
    public boolean step() {
        EntityManager em = context.get(EntityManager.class);
        boolean entityCreated = false;
        for (EntityRef entity : em.getAllEntities()) {
            entityCreated = true;
            logger.atError().log("Entity created before load: {}", entity.toFullDescription());
        }
        if (entityCreated) {
            throw new IllegalStateException("Entity creation detected during component system initialisation, game load aborting");
        }
        StorageManager storageManager = context.get(StorageManager.class);
        try {
            storageManager.loadGlobalStore();
        } catch (IOException e) {
            logger.error("Failed to load global data.", e);
        }
        return true;
    }

    @Override
    public int getExpectedCost() {
        return 1;
    }


}

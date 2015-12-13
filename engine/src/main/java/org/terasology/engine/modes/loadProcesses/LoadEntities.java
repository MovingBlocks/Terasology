/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.engine.modes.loadProcesses;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.context.Context;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.persistence.StorageManager;

import java.io.IOException;

/**
 */
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
            logger.error("Entity created before load: {}", entity.toFullDescription());
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

/*
 * Copyright 2013 Moving Blocks
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
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.persistence.WorldPersister;
import org.terasology.engine.CoreRegistry;
import org.terasology.engine.TerasologyConstants;
import org.terasology.engine.modes.LoadProcess;
import org.terasology.engine.paths.PathManager;
import org.terasology.world.WorldInfo;

import java.io.File;
import java.io.IOException;

/**
 * @author Immortius
 */
public class LoadEntities implements LoadProcess {

    private static final Logger logger = LoggerFactory.getLogger(LoadEntities.class);

    private WorldInfo worldInfo;

    public LoadEntities(WorldInfo worldInfo) {
        this.worldInfo = worldInfo;
    }

    @Override
    public String getMessage() {
        return "Loading Entities";
    }

    @Override
    public boolean step() {
        CoreRegistry.put(WorldPersister.class, new WorldPersister(CoreRegistry.get(EntityManager.class)));

        // TODO: Should probably not use the world title as a path?
        File entityDataFile = new File(PathManager.getInstance().getWorldSavePath(worldInfo.getTitle()), TerasologyConstants.ENTITY_DATA_FILE);
        if (entityDataFile.exists()) {
            try {
                CoreRegistry.get(WorldPersister.class).load(entityDataFile, WorldPersister.SaveFormat.Binary);
            } catch (IOException e) {
                logger.error("Failed to load entity data", e);
            }
        }
        return true;
    }

    @Override
    public int begin() {
        return 1;
    }
}

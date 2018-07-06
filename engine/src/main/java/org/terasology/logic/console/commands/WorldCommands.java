/*
 * Copyright 2018 MovingBlocks
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
package org.terasology.logic.console.commands;

import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.internal.EngineEntityPool;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.console.commandSystem.annotations.Command;
import org.terasology.logic.console.commandSystem.annotations.CommandParam;
import org.terasology.registry.In;
import org.terasology.world.internal.WorldInfo;

import java.util.HashMap;
import java.util.Map;

@RegisterSystem
public class WorldCommands extends BaseComponentSystem {

    @In
    private EntityManager entityManager;

    @Command(shortDescription = "Get information about different worlds and " +
            "entities present in each pool", runOnServer = true)
    public String getUniverseInfo() {
        int worldCount = entityManager.getWorldPools().size();
        StringBuilder message = new StringBuilder(100);
        message.append("Number of world is " + worldCount + "\n");
        Map<Long, EngineEntityPool> worldPoolMap = entityManager.getPoolMap();
        Map<EngineEntityPool, Long> poolCounts = new HashMap<EngineEntityPool, Long>();
        for (EngineEntityPool engineEntityPools : entityManager.getWorldPools()) {
            poolCounts.put(engineEntityPools, 0L);
        }
        for (Map.Entry<Long, EngineEntityPool> entry : worldPoolMap.entrySet()) {
            if(poolCounts.containsKey(entry.getValue())) {
                poolCounts.put(entry.getValue(), poolCounts.get(entry.getValue()) + 1);
            } else {
                poolCounts.put(entry.getValue(), 1L);
            }

        }
        for (Map.Entry<WorldInfo, EngineEntityPool> entry : entityManager.getWorldPoolsMap().entrySet()) {
            message.append("Pool for " + entry.getKey().getTitle() + " has " + poolCounts.get(entry.getValue()) + " entities \n");
        }
        return message.toString();
    }

    @Command(shortDescription = "Get information about different worlds and " +
            "entities present in each pool", runOnServer = true)
    public String makeEntity(@CommandParam("The world in which the entity is formed") String worldName) {
        for (Map.Entry<WorldInfo, EngineEntityPool> entry : entityManager.getWorldPoolsMap().entrySet()) {
            if(entry.getKey().getTitle().equalsIgnoreCase(worldName)) {
                entry.getValue().create();
                return "Entity created in " + entry.getKey().getTitle() + " world";
            }
        }

        return  worldName + " does not exist";
    }


}

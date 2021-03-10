// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.console.commands;

import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.internal.EngineEntityPool;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.console.commandSystem.annotations.Command;
import org.terasology.engine.logic.console.commandSystem.annotations.CommandParam;
import org.terasology.engine.logic.console.commandSystem.annotations.Sender;
import org.terasology.engine.logic.chat.ChatMessageEvent;
import org.terasology.engine.logic.common.DisplayNameComponent;
import org.terasology.engine.network.ColorComponent;
import org.terasology.nui.Color;
import org.terasology.engine.registry.In;
import org.terasology.engine.world.internal.WorldInfo;

import java.util.Map;

@RegisterSystem
public class WorldCommands extends BaseComponentSystem {

    @In
    private EntityManager entityManager;


    @Command(shortDescription = "Get information about different worlds and " +
            "entities present in each pool", runOnServer = true)
    public String getUniverseInfo() {
        int worldCount = entityManager.getWorldPools().size();
        StringBuilder message = new StringBuilder();
        message.append("Number of worlds is " + worldCount + "\n");
        for (Map.Entry<WorldInfo, EngineEntityPool> entry : entityManager.getWorldPoolsMap().entrySet()) {
            message.append("Pool for " + entry.getKey().getTitle() + " has " + entry.getValue().getActiveEntityCount() + " entities \n");
        }
        return message.toString();
    }

    @Command(shortDescription = "Make new entity in a given pool", runOnServer = true)
    public String makeEntity(@CommandParam("The world in which the entity is formed") String worldName) {
        for (Map.Entry<WorldInfo, EngineEntityPool> entry : entityManager.getWorldPoolsMap().entrySet()) {
            if (entry.getKey().getTitle().equalsIgnoreCase(worldName)) {
                EntityRef entityRef = entry.getValue().create();
                return "Entity created in " + entry.getKey().getTitle() + " world with id " + entityRef.getId();
            }
        }

        return worldName + " does not exist";
    }

    @Command(shortDescription = "Moves the last created entity to another pool ", runOnServer = true)
    public String moveEntity(@CommandParam("The world in which the entity is formed") String worldName,
                             @CommandParam("Id of the entity to be moved") int id) {

        for (Map.Entry<WorldInfo, EngineEntityPool> entry : entityManager.getWorldPoolsMap().entrySet()) {
            if (entry.getKey().getTitle().equalsIgnoreCase(worldName)) {
                if (entityManager.moveToPool(id, entry.getValue())) {
                    return "Entity " + id + " moved to " + entry.getKey().getTitle() + "world";
                } else {
                    return "Entity" + id + "could not be moved";
                }
            }
        }

        return worldName + " does not exist";
    }

    @Command(shortDescription = "Check which pool an entity is in", runOnServer = true)
    public String whereIs(@CommandParam("entity id") long id) {
        Map<Long, EngineEntityPool> worldPoolMap = entityManager.getPoolMap();
        if (worldPoolMap.containsKey(id)) {
            EngineEntityPool pool = worldPoolMap.get(id);
            for (Map.Entry<WorldInfo, EngineEntityPool> entry : entityManager.getWorldPoolsMap().entrySet()) {
                if (entry.getValue() == pool) {
                    return "Entity" + id + " is present in " + entry.getKey().getTitle();
                }
            }
        }
        return "Entity" + id + " not found";
    }

    @Command(shortDescription = "Random", runOnServer = true)
    public String simulate(@Sender EntityRef sender) {
        EntityRef simulatedEntity = entityManager.create("engine:multiWorldSim");


        DisplayNameComponent displayNameComponent = simulatedEntity.getComponent(DisplayNameComponent.class);
        displayNameComponent.name = "I-Travel-Worlds-" + simulatedEntity.getId();
        simulatedEntity.saveComponent(displayNameComponent);

        ColorComponent colorComponent = simulatedEntity.getComponent(ColorComponent.class);
        colorComponent.color = Color.RED;
        simulatedEntity.saveComponent(colorComponent);
        sender.send(new ChatMessageEvent("yay", simulatedEntity));

        return "done";

    }

}

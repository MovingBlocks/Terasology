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
package org.terasology.logic.console.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.GameEngine;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.common.DisplayNameComponent;
import org.terasology.logic.console.Command;
import org.terasology.network.Client;
import org.terasology.network.ClientComponent;
import org.terasology.network.NetworkSystem;
import org.terasology.registry.CoreRegistry;
import org.terasology.registry.In;

/**
 * Commands to administer a remote server
 * @author Martin Steiger
 */
@RegisterSystem
public class ServerCommands extends BaseComponentSystem {

    private static final Logger logger = LoggerFactory.getLogger(ServerCommands.class);

    @In
    private EntityManager entityManager;
    
    @Command(shortDescription = "Shutdown the server", runOnServer = true)
    public String shutdownServer(EntityRef sender) {
        EntityRef clientInfo = sender.getComponent(ClientComponent.class).clientInfo;

        DisplayNameComponent name = clientInfo.getComponent(DisplayNameComponent.class);
        
        logger.info("Shutdown triggered by {}", name.name);
        
        CoreRegistry.get(GameEngine.class).shutdown();
        
        return "Server shutdown triggered";
    }
    
    @Command(shortDescription = "Kick user", runOnServer = true)
    public String kick(String username, EntityRef sender) {
        NetworkSystem network = CoreRegistry.get(NetworkSystem.class);

        for (EntityRef clientEntity : entityManager.getEntitiesWith(ClientComponent.class)) {
            EntityRef clientInfo = sender.getComponent(ClientComponent.class).clientInfo;

            DisplayNameComponent name = clientInfo.getComponent(DisplayNameComponent.class);
            if (username.equals(name.name)) {
                
                Client client = network.getOwner(clientEntity);
        
                if (!client.isLocal()) {
                    logger.info("Kicking user {}", name.name);

                    network.forceDisconnect(client);
                    return "User kick triggered for '" + username + "'";
                }

            }
        }
        
        throw new IllegalArgumentException("No such user '" + username + "'");
    }

}


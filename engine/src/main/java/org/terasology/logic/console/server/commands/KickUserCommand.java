/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.logic.console.server.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.common.DisplayNameComponent;
import org.terasology.logic.console.internal.Command;
import org.terasology.logic.console.internal.CommandParameter;
import org.terasology.network.Client;
import org.terasology.network.ClientComponent;
import org.terasology.network.NetworkSystem;
import org.terasology.registry.CoreRegistry;
import org.terasology.registry.In;

/**
 * @author Martin Steiger, Limeth
 */
@RegisterSystem
public class KickUserCommand extends Command {
    private static final Logger logger = LoggerFactory.getLogger(KickUserCommand.class);

    @In
    private EntityManager entityManager;

    public KickUserCommand() {
        super("kickUser", true, "Kick user by name", null);
    }

    @Override
    protected CommandParameter[] constructParameters() {
        return new CommandParameter[] {
                CommandParameter.single("username", String.class, true)
        };
    }

    public String execute(EntityRef sender, String username) {
        // TODO: verify permissions of sender

        for (EntityRef clientEntity : entityManager.getEntitiesWith(ClientComponent.class)) {
            EntityRef clientInfo = clientEntity.getComponent(ClientComponent.class).clientInfo;

            DisplayNameComponent name = clientInfo.getComponent(DisplayNameComponent.class);
            if (username.equals(name.name)) {

                return kick(clientEntity);
            }
        }

        throw new IllegalArgumentException("No such user '" + username + "'");
    }

    public static String kick(EntityRef clientEntity) {
        NetworkSystem network = CoreRegistry.get(NetworkSystem.class);
        Client client = network.getOwner(clientEntity);

        if (!client.isLocal()) {
            EntityRef clientInfo = clientEntity.getComponent(ClientComponent.class).clientInfo;
            DisplayNameComponent name = clientInfo.getComponent(DisplayNameComponent.class);

            logger.info("Kicking user {}", name.name);

            network.forceDisconnect(client);
            return "User kick triggered for '" + name.name + "'";
        }

        return "Request declined";
    }

    //TODO Add command completion via the suggest method
}

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

import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.console.internal.Command;
import org.terasology.logic.console.internal.CommandParameter;
import org.terasology.network.ClientComponent;
import org.terasology.network.NetworkComponent;
import org.terasology.registry.In;

/**
 * @author Martin Steiger, Limeth
 */
@RegisterSystem
public class KickUserByIDCommand extends Command {
    @In
    private EntityManager entityManager;

    public KickUserByIDCommand() {
        super("kickUserById", true, "Kick user by ID", null);
    }

    @Override
    protected CommandParameter[] constructParameters() {
        return new CommandParameter[] {
                CommandParameter.single("userId", Integer.class, true)
        };
    }

    public String execute(EntityRef sender, Integer userId) {
        // TODO: verify permissions of sender

        for (EntityRef clientEntity : entityManager.getEntitiesWith(ClientComponent.class)) {
            EntityRef clientInfo = clientEntity.getComponent(ClientComponent.class).clientInfo;
            NetworkComponent nc = clientInfo.getComponent(NetworkComponent.class);

            if (userId == nc.getNetworkId()) {
                return KickUserCommand.kick(clientEntity);
            }
        }

        throw new IllegalArgumentException("No such user with ID " + userId);
    }

    //TODO Add command completion via the suggest method
}

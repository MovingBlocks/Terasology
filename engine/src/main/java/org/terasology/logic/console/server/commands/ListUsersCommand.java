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

/**
 * @author Limeth
 */

import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.common.DisplayNameComponent;
import org.terasology.logic.console.Message;
import org.terasology.logic.console.dynamic.Command;
import org.terasology.logic.console.dynamic.CommandParameter;
import org.terasology.network.ClientInfoComponent;
import org.terasology.network.ColorComponent;
import org.terasology.network.NetworkComponent;
import org.terasology.registry.In;
import org.terasology.rendering.FontColor;

/**
 * @author Martin Steiger, Limeth
 */
@RegisterSystem
public class ListUsersCommand extends Command {
    @In
    private EntityManager entityManager;

    public ListUsersCommand() {
        super("listUsers", false, "Lists users", null);
    }

    @Override
    protected CommandParameter[] constructParameters() {
        return new CommandParameter[0];
    }

    public String execute(EntityRef sender) {
        StringBuilder stringBuilder = new StringBuilder();

        for (EntityRef clientInfo : entityManager.getEntitiesWith(ClientInfoComponent.class)) {

            DisplayNameComponent dnc = clientInfo.getComponent(DisplayNameComponent.class);
            ColorComponent cc = clientInfo.getComponent(ColorComponent.class);
            NetworkComponent nc = clientInfo.getComponent(NetworkComponent.class);

            String playerText = FontColor.getColored(dnc.name, cc.color);
            String line = String.format("%s - %s (%d)", playerText, dnc.description, nc.getNetworkId());

            stringBuilder.append(line);
            stringBuilder.append(Message.NEW_LINE);
        }

        return stringBuilder.toString();
    }
}

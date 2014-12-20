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
package org.terasology.logic.chat.commands;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.chat.ChatMessageEvent;
import org.terasology.logic.common.DisplayNameComponent;
import org.terasology.logic.console.ConsoleColors;
import org.terasology.logic.console.internal.Command;
import org.terasology.logic.console.internal.CommandParameter;
import org.terasology.logic.console.internal.CommandParameterSuggester;
import org.terasology.network.ClientComponent;
import org.terasology.registry.CoreRegistry;
import org.terasology.registry.In;
import org.terasology.rendering.FontColor;

import java.util.Set;

/**
 * @author Limeth
 */
@RegisterSystem
public class TellCommand extends Command {
    @In
    private EntityManager entityManager;

    public TellCommand() {
        super("tell", true, "Sends a private message to a specified user", null);
    }

    @Override
    protected CommandParameter[] constructParameters() {
        return new CommandParameter[] {
                CommandParameter.single("user", String.class, true, UsernameCommandParameterSuggester.class),
                CommandParameter.varargs("message", String.class, true)
        };
    }

    public String execute(EntityRef sender, String username, String[] messageArray) {
        Iterable<EntityRef> clients = entityManager.getEntitiesWith(ClientComponent.class);
        EntityRef targetClient = null;
        boolean unique = true;

        for (EntityRef client : clients) {
            ClientComponent clientComponent = client.getComponent(ClientComponent.class);
            DisplayNameComponent displayNameComponent = clientComponent.clientInfo.getComponent(DisplayNameComponent.class);

            if (displayNameComponent == null) {
                continue;
            }

            if (displayNameComponent.name.equalsIgnoreCase(username)) {
                if (targetClient == null) {
                    targetClient = client;
                } else {
                    unique = false;
                    break;
                }
            }
        }

        if (!unique) {
            targetClient = null;

            for (EntityRef client : clients) {
                ClientComponent clientComponent = client.getComponent(ClientComponent.class);
                DisplayNameComponent displayNameComponent = clientComponent.clientInfo.getComponent(DisplayNameComponent.class);

                if (displayNameComponent == null) {
                    continue;
                }

                if (displayNameComponent.name.equals(username)) {
                    if (targetClient == null) {
                        targetClient = client;
                    } else {
                        return FontColor.getColored("Found more users with name '" + username + "'.", ConsoleColors.ERROR);
                    }
                }
            }
        }

        if (targetClient == null) {
            return FontColor.getColored("User with name '" + username + "' not found.", ConsoleColors.ERROR);
        }

        ClientComponent senderClientComponent = sender.getComponent(ClientComponent.class);
        DisplayNameComponent senderDisplayNameComponent = senderClientComponent.clientInfo.getComponent(DisplayNameComponent.class);
        ClientComponent targetClientComponent = targetClient.getComponent(ClientComponent.class);
        DisplayNameComponent targetDisplayNameComponent = targetClientComponent.clientInfo.getComponent(DisplayNameComponent.class);
        String message = Joiner.on(' ').join(messageArray);
        String targetMessage = FontColor.getColored("*whispering* ", ConsoleColors.ERROR)
                + FontColor.getColored(message, ConsoleColors.CHAT);
        String senderMessage = "You -> " + targetDisplayNameComponent.name
                + ": " + FontColor.getColored(message, ConsoleColors.CHAT);

        targetClient.send(new ChatMessageEvent(targetMessage, senderClientComponent.clientInfo));

        return senderMessage;
    }

    public static class UsernameCommandParameterSuggester implements CommandParameterSuggester<String> {
        public String[] suggest(EntityRef sender, Object[] parameters) {
            EntityManager entityManager = CoreRegistry.get(EntityManager.class);
            Iterable<EntityRef> clients = entityManager.getEntitiesWith(ClientComponent.class);
            Set<String> clientNames = Sets.newHashSet();

            for (EntityRef client : clients) {
                ClientComponent clientComponent = client.getComponent(ClientComponent.class);
                DisplayNameComponent displayNameComponent = clientComponent.clientInfo.getComponent(DisplayNameComponent.class);

                clientNames.add(displayNameComponent.name);
            }

            return clientNames.toArray(new String[clientNames.size()]);
        }
    }
}

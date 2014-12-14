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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.chat.ChatMessageEvent;
import org.terasology.logic.console.ConsoleColors;
import org.terasology.logic.console.dynamic.Command;
import org.terasology.logic.console.dynamic.CommandParameter;
import org.terasology.network.ClientComponent;
import org.terasology.registry.In;
import org.terasology.rendering.FontColor;

/**
 * This commands provides the ability to chat. Chat messages are broadcast to all players.
 *
 * @author Immortius, Limeth
 */
@RegisterSystem
public class SayCommand extends Command {
    private static final Logger logger = LoggerFactory.getLogger(SayCommand.class);

    @In
    private EntityManager entityManager;

    public SayCommand() {
        super("say", true, "Sends a message to all other players", null);
    }

    @Override
    protected CommandParameter[] constructParameters() {
        return new CommandParameter[] {
                CommandParameter.varargs("message", String.class)
        };
    }

    public String execute(EntityRef sender, String[] messageArray) {
        if(messageArray == null) {
            return FontColor.getColored("Provide a message to send, please.", ConsoleColors.ERROR);
        }

        String message = Joiner.on(' ').join(messageArray);

        logger.debug("Received chat message from {} : '{}'", sender, message);

        for (EntityRef client : entityManager.getEntitiesWith(ClientComponent.class)) {
            client.send(new ChatMessageEvent(message, sender.getComponent(ClientComponent.class).clientInfo));
        }

        return "Message sent.";
    }
}

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
import org.terasology.engine.GameEngine;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.common.DisplayNameComponent;
import org.terasology.logic.console.dynamic.Command;
import org.terasology.logic.console.dynamic.CommandParameter;
import org.terasology.network.ClientComponent;
import org.terasology.registry.CoreRegistry;

/**
 * @author Martin Steiger, Limeth
 */
@RegisterSystem
public class ShutdownServerCommand extends Command {
    private static final Logger logger = LoggerFactory.getLogger(ShutdownServerCommand.class);

    public ShutdownServerCommand() {
        super("shutdownServer", true, "Shutdown the server", null);
    }

    @Override
    protected CommandParameter[] constructParameters() {
        return new CommandParameter[0];
    }

    public String execute(EntityRef sender) {
        // TODO: verify permissions of sender

        EntityRef clientInfo = sender.getComponent(ClientComponent.class).clientInfo;
        DisplayNameComponent name = clientInfo.getComponent(DisplayNameComponent.class);

        logger.info("Shutdown triggered by {}", name.name);

        CoreRegistry.get(GameEngine.class).shutdown();

        return "Server shutdown triggered";
    }
}

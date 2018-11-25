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
package org.terasology.entitySystem.systems;

import org.terasology.engine.subsystem.rpc.DiscordRPCSubSystem;
import org.terasology.game.Game;
import org.terasology.registry.In;

/**
 * It's a system that runs when a single player or multi player game has been started to process some stuff
 * throw the {@link DiscordRPCSubSystem}.
 *
 * @see DiscordRPCSubSystem
 */
@RegisterSystem(RegisterMode.ALWAYS)
public class DiscordRPCSystem extends BaseComponentSystem {

    @In
    private Game game;

    public String getGame() {
        return String.format("In Game | %s", game.getName());
    }

    @Override
    public void initialise() {
        DiscordRPCSubSystem.tryToDiscover();
    }

    @Override
    public void preBegin() {
        DiscordRPCSubSystem.setState(getGame(), false);
    }

    @Override
    public void postBegin() {
        DiscordRPCSubSystem.setState(getGame(), true);
    }

    @Override
    public void shutdown() {
        DiscordRPCSubSystem.setState("In Lobby");
    }
}

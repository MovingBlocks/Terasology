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
package org.terasology.engine.subsystem.discordrpc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.config.Config;
import org.terasology.config.PlayerConfig;
import org.terasology.context.Context;
import org.terasology.engine.GameEngine;
import org.terasology.engine.subsystem.EngineSubsystem;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.time.OffsetDateTime;

/**
 * Subsystem that manages Discord RPC in the game client, such as status or connection.
 * This subsystem can be enhanced further to improve game presentation in rich presence.
 *
 * It communicates with the thread safely using thread-safe shared buffer.
 *
 * @see EngineSubsystem
 */
public final class DiscordRPCSubSystem implements EngineSubsystem, PropertyChangeListener {
    private static final Logger logger = LoggerFactory.getLogger(DiscordRPCSubSystem.class);
    private static DiscordRPCSubSystem instance;

    private Config config;
    private DiscordRPCThread thread;

    public DiscordRPCSubSystem() throws IllegalStateException {
        if (instance != null) {
            throw new IllegalStateException("More then one instance in the DiscordRPC");
        }

        instance = this;
    }

    @Override
    public void initialise(GameEngine engine, Context rootContext) {
        logger.info("Initializing...");

        thread = new DiscordRPCThread();
        thread.getBuffer().setState("In Main Menu");

        config = rootContext.get(Config.class);

        if (config.getPlayer().isDiscordPresence()) {
            thread.enable();
        } else {
            logger.info("Discord RPC is disabled! No connection is being made during initialization.");
            thread.disable();
        }
        thread.start();
    }

    @Override
    public synchronized void postInitialise(Context context) {
        config = context.get(Config.class);
        config.getPlayer().subscribe(this);

        if (config.getPlayer().isDiscordPresence()) {
            thread.enable();
        } else {
            thread.disable();
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(PlayerConfig.DISCORD_PRESENCE)) {
            thread.setEnabled((boolean) evt.getNewValue());
        }
    }

    @Override
    public synchronized void preShutdown() {
        thread.disable();
        thread.stop();
    }

    @Override
    public String getName() {
        return "DiscordRPC";
    }

    /**
     * Re-discovers the discord ipc in case the player started the discord client after running the game.
     * And, the re-connecting process failed to connect.
     *
     * This should be called once by {@link DiscordRPCSystem}
     */
    public static void discover() {
        getInstance().thread.discover();
    }

    /**
     * Resets the current rich presence data
     */
    public static void reset() {
        getInstance().thread.getBuffer().reset();
    }

    /**
     * Sets the name of the gameplay the player is playing (e.g. Custom, Josharias Survival, etc...)
     * @param name the name of the gameplay
     */
    public static void setGameplayName(String name) {
        getInstance().thread.getBuffer().setDetails("Game: " + name);
    }

    /**
     * Sets the current game/party status for the player (e.g. Playing Solo, Idle, etc...)
     *
     * @param state The current game/party status
     */
    public static void setState(String state) {
        getInstance().thread.getBuffer().setState(state);
    }

    /**
     * Sets an elapsed time since the player's state
     *
     * @param timestamp The elapsed time since player's action. `null` to disable it.
     */
    public static void setStartTimestamp(OffsetDateTime timestamp) {
        getInstance().thread.getBuffer().setStartTimestamp(timestamp);
    }

    /**
     * Sets the party information on the buffer
     *
     * @param size The number of the players in the party
     * @param max The maximum number of the players in the party
     */
    public static void setPartyInfo(int size, int max) {
        DiscordRPCBuffer buffer = getInstance().thread.getBuffer();
        buffer.setPartySize(size);
        buffer.setPartyMax(max);
    }

    /**
     * Resets the party information on the buffer
     */
    public static void resetPartyInfo() {
        setPartyInfo(-1, -1);
    }

    private static DiscordRPCSubSystem getInstance() {
        return instance;
    }
}

// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.subsystem.discordrpc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.GameEngine;
import org.terasology.engine.core.subsystem.EngineSubsystem;

import java.time.OffsetDateTime;

/**
 * Subsystem that manages Discord RPC in the game client, such as status or connection. This subsystem can be enhanced
 * further to improve game presentation in rich presence.
 * <p>
 * It communicates with the thread safely using thread-safe shared buffer.
 *
 * @see EngineSubsystem
 */
public final class DiscordRPCSubSystem implements EngineSubsystem {
    private static final Logger logger = LoggerFactory.getLogger(DiscordRPCSubSystem.class);
    private static DiscordRPCSubSystem instance;

    private DiscordAutoConfig config;
    private DiscordRPCThread thread;

    public DiscordRPCSubSystem() throws IllegalStateException {
        if (instance != null) {
            throw new IllegalStateException("More then one instance in the DiscordRPC");
        }

        instance = this;
    }

    /**
     * Re-discovers the discord ipc in case the player started the discord client after running the game. And, the
     * re-connecting process failed to connect.
     * <p>
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
     *
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

    @Override
    public void initialise(GameEngine engine, Context rootContext) {
        logger.info("Initializing...");

        thread = new DiscordRPCThread();
        thread.getBuffer().setState("In Main Menu");

        config = rootContext.get(DiscordAutoConfig.class);

        if (config.discordPresence.get()) {
            thread.enable();
        } else {
            logger.info("Discord RPC is disabled! No connection is being made during initialization.");
            thread.disable();
        }
        thread.start();
    }

    @Override
    public synchronized void postInitialise(Context context) {
        config = context.get(DiscordAutoConfig.class);
        config.discordPresence.subscribe((setting, old) -> {
            if (setting.get()) {
                thread.enable();
            } else {
                thread.disable();
            }
        });
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
}

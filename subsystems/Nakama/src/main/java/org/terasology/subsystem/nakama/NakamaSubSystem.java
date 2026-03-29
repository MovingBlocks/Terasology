// Copyright 2026 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.subsystem.nakama;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.heroiclabs.nakama.AbstractSocketListener;
import com.heroiclabs.nakama.Channel;
import com.heroiclabs.nakama.api.ChannelMessage;
import com.heroiclabs.nakama.ChannelType;
import com.heroiclabs.nakama.Client;
import com.heroiclabs.nakama.DefaultClient;
import com.heroiclabs.nakama.Session;
import com.heroiclabs.nakama.SocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.ComponentSystemManager;
import org.terasology.engine.core.GameEngine;
import org.terasology.engine.core.modes.GameState;
import org.terasology.engine.core.subsystem.EngineSubsystem;
import org.terasology.engine.logic.console.Console;
import org.terasology.engine.logic.console.CoreMessageType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

/**
 * Optional engine subsystem that bridges Terasology chat to a Nakama
 * chat channel, enabling cross-game messaging for the Bifrost protocol.
 *
 * Enable via the Nakama config file at ~/.terasology/configs/nakama/NakamaAutoConfig.cfg
 */
public class NakamaSubSystem implements EngineSubsystem {
    private static final Logger logger = LoggerFactory.getLogger(NakamaSubSystem.class);
    private static final String GAME_ID = "terasology";
    private static final Map<String, String> GAME_PREFIXES = Map.of(
            "terasology", "TS", "destinationsol", "DS", "minecraft", "MC"
    );

    private NakamaAutoConfig autoConfig;
    private Client client;
    private Session session;
    private SocketClient socket;
    private Channel channel;

    // Thread-safe queue for incoming messages — drained on the game thread in preUpdate
    private final ConcurrentLinkedQueue<String> incomingMessages = new ConcurrentLinkedQueue<>();
    private Console console;

    // Callback for incoming messages — set by the engine/module that handles chat display
    private Consumer<String> incomingMessageHandler;

    // Flag to prevent re-forwarding messages we injected
    private volatile boolean suppressOutbound = false;

    @Override
    public String getName() {
        return "Nakama";
    }

    @Override
    public void initialise(GameEngine engine, Context rootContext) {
        autoConfig = rootContext.get(NakamaAutoConfig.class);
        if (autoConfig == null) {
            logger.warn("Nakama subsystem: NakamaAutoConfig not found in context — AutoConfig discovery may have failed");
            return;
        }
        if (!autoConfig.enabled.get()) {
            logger.info("Nakama subsystem disabled (set enabled=true in config file)");
            return;
        }
        connect();
    }

    private void connect() {
        try {
            String deviceId = getOrCreateDeviceId();
            client = new DefaultClient("defaultkey", autoConfig.host.get(), autoConfig.grpcPort.get(), false);
            session = client.authenticateDevice(deviceId).get();
            logger.info("Nakama: authenticated as {}", session.getUserId());

            socket = client.createSocket(autoConfig.host.get(), autoConfig.wsPort.get(), false);
            socket.connect(session, new AbstractSocketListener() {
                @Override
                public void onChannelMessage(ChannelMessage message) {
                    handleIncomingMessage(message);
                }
            }).get();

            // Join the shared chat channel
            channel = socket.joinChat(autoConfig.channel.get(), ChannelType.ROOM).get();
            logger.info("Nakama: joined channel '{}'", autoConfig.channel.get());

        } catch (Exception e) {
            logger.warn("Nakama: connection failed, continuing without cross-game chat", e);
            cleanup();
        }
    }

    private void handleIncomingMessage(ChannelMessage message) {
        try {
            JsonObject content = JsonParser.parseString(message.getContent()).getAsJsonObject();
            String game = content.has("game") ? content.get("game").getAsString() : "";
            // Echo filter: ignore our own game's messages
            if (GAME_ID.equals(game)) {
                return;
            }
            String player = content.has("player") ? content.get("player").getAsString() : "???";
            String text = content.has("text") ? content.get("text").getAsString() : "";
            String prefix = "[" + GAME_PREFIXES.getOrDefault(game,
                    game.toUpperCase().substring(0, Math.min(game.length(), 2))) + "]";
            String formatted = prefix + " " + player + ": " + text;

            logger.info("Nakama chat: {}", formatted);
            incomingMessages.add(formatted);

            if (incomingMessageHandler != null) {
                suppressOutbound = true;
                try {
                    incomingMessageHandler.accept(formatted);
                } finally {
                    suppressOutbound = false;
                }
            }
        } catch (Exception e) {
            logger.warn("Nakama: failed to parse incoming message", e);
        }
    }

    /**
     * Send a chat message to the Nakama channel.
     * Called by the chat system when a local player sends a message.
     * Returns true if the message was sent, false if suppressed or not connected.
     */
    public boolean sendChatMessage(String playerName, String text) {
        if (suppressOutbound || socket == null || channel == null) {
            return false;
        }
        try {
            JsonObject content = new JsonObject();
            content.addProperty("game", GAME_ID);
            content.addProperty("player", playerName);
            content.addProperty("text", text);
            socket.writeChatMessage(channel.getId(), content.toString()).get();
            return true;
        } catch (Exception e) {
            logger.warn("Nakama: failed to send message", e);
            return false;
        }
    }

    /**
     * Register a handler for incoming cross-game messages.
     * The handler receives a pre-formatted string like "[DS] Bob: Hello"
     */
    public void setIncomingMessageHandler(Consumer<String> handler) {
        this.incomingMessageHandler = handler;
    }

    @Override
    public void registerSystems(ComponentSystemManager componentSystemManager) {
        if (autoConfig != null && autoConfig.enabled.get()) {
            NakamaSystem nakamaSystem = new NakamaSystem();
            nakamaSystem.setNakamaSubSystem(this);
            componentSystemManager.register(nakamaSystem);
        }
    }

    @Override
    public void postInitialise(Context context) {
        if (autoConfig == null || !autoConfig.enabled.get() || !isConnected()) {
            return;
        }
        console = context.get(Console.class);
    }

    @Override
    public void preUpdate(GameState currentState, float delta) {
        String msg;
        while ((msg = incomingMessages.poll()) != null) {
            // Lazily resolve console — it's only available once a game world is loaded
            if (console == null) {
                console = currentState.getContext().get(Console.class);
            }
            if (console != null) {
                console.addMessage(msg, CoreMessageType.CHAT);
            } else {
                logger.info("Nakama chat (no console yet): {}", msg);
            }
        }
    }

    public boolean isConnected() {
        return socket != null && channel != null;
    }

    @Override
    public void preShutdown() {
        cleanup();
    }

    private void cleanup() {
        if (socket != null) {
            try { socket.disconnect(); } catch (Exception ignored) { }
            socket = null;
        }
        channel = null;
        session = null;
        client = null;
    }

    private String getOrCreateDeviceId() {
        String id = System.getProperty("nakama.deviceId", "");
        if (!id.isEmpty()) {
            return id;
        }
        // Persist device ID to a file so we get the same Nakama user across restarts
        Path idFile = Paths.get(System.getProperty("user.home"), ".bifrost", "device-id");
        try {
            if (Files.exists(idFile)) {
                id = Files.readString(idFile).trim();
                if (!id.isEmpty()) {
                    return id;
                }
            }
            id = UUID.randomUUID().toString();
            Files.createDirectories(idFile.getParent());
            Files.writeString(idFile, id);
            logger.info("Nakama: created device ID {}", id);
        } catch (IOException e) {
            id = UUID.randomUUID().toString();
            logger.warn("Nakama: could not persist device ID, using ephemeral {}", id);
        }
        return id;
    }
}

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
import org.terasology.engine.core.subsystem.EngineSubsystem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Optional engine subsystem that bridges Terasology chat to a Nakama
 * chat channel, enabling cross-game messaging for the Bifrost protocol.
 *
 * Enable via system property: -Dnakama.enabled=true -Dnakama.host=192.168.x.x
 */
public class NakamaSubSystem implements EngineSubsystem {
    private static final Logger logger = LoggerFactory.getLogger(NakamaSubSystem.class);
    private static final String GAME_ID = "terasology";
    private static final Map<String, String> GAME_PREFIXES = Map.of(
            "terasology", "TS", "destinationsol", "DS", "minecraft", "MC"
    );

    private NakamaConfig config;
    private Client client;
    private Session session;
    private SocketClient socket;
    private Channel channel;

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
        config = NakamaConfig.fromSystemProperties();
        if (!config.isEnabled()) {
            logger.info("Nakama subsystem disabled (set -Dnakama.enabled=true to enable)");
            return;
        }
        connect();
    }

    private void connect() {
        try {
            String deviceId = getOrCreateDeviceId();
            // DefaultClient(serverKey, host, port, ssl) — port is HTTP/WebSocket port
            client = new DefaultClient("defaultkey", config.getHost(), config.getPort(), false);
            session = client.authenticateDevice(deviceId).get();
            logger.info("Nakama: authenticated as {}", session.getUserId());

            socket = client.createSocket();
            socket.connect(session, new AbstractSocketListener() {
                @Override
                public void onChannelMessage(ChannelMessage message) {
                    handleIncomingMessage(message);
                }
            }).get();

            // Join the shared chat channel
            channel = socket.joinChat(config.getChannel(), ChannelType.ROOM).get();
            logger.info("Nakama: joined channel '{}'", config.getChannel());

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
        if (config != null && config.isEnabled()) {
            NakamaSystem nakamaSystem = new NakamaSystem();
            nakamaSystem.setNakamaSubSystem(this);
            componentSystemManager.register(nakamaSystem);
        }
    }

    @Override
    public void postInitialise(Context context) {
        if (config == null || !config.isEnabled() || !isConnected()) {
            return;
        }
        // Inbound: inject Nakama messages into the local chat system
        setIncomingMessageHandler(formatted -> {
            // For POC, log to the game log. Full chat injection requires
            // accessing the NUI ChatBox or sending a synthetic ChatMessageEvent.
            logger.info("Nakama chat: {}", formatted);
        });
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

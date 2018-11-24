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
package org.terasology.engine.subsystem.rpc;

import com.jagrosh.discordipc.IPCClient;
import com.jagrosh.discordipc.IPCListener;
import com.jagrosh.discordipc.entities.RichPresence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.config.Config;
import org.terasology.context.Context;
import org.terasology.engine.GameEngine;
import org.terasology.engine.subsystem.EngineSubsystem;

import java.time.OffsetDateTime;

public class DiscordRPCSubSystem implements EngineSubsystem, IPCListener, Runnable {

    private static DiscordRPCSubSystem instance;

    private Logger logger;
    private IPCClient ipcClient;
    private boolean ready;
    private boolean autoReconnect;
    private Thread reconnectThread;
    private RichPresence lastRichPresence;
    private boolean reconnecting;
    private int reconnectTries = 1;
    private boolean connectedBefore;
    private int lastPing;
    private Config config;
    private String lastState;

    public DiscordRPCSubSystem() throws IllegalStateException {
        if (instance != null) {
            throw new IllegalStateException("More then one instance in the DiscordRPC");
        }
        lastRichPresence = null;
        ipcClient = new IPCClient(515274721080639504L);
        ipcClient.setListener(this);
        logger = LoggerFactory.getLogger(DiscordRPCSubSystem.class);
        autoReconnect = true;
        reconnectThread = new Thread(this);
        reconnectThread.setName("DISCORD-RPC-RECONNECT");
        reconnectThread.start();
        instance = this;
    }

    public void sendRichPresence(RichPresence richPresence) {
        this.lastRichPresence = richPresence;
        if (!ready || lastRichPresence == null) {
            return;
        }
        ipcClient.sendRichPresence(lastRichPresence);
    }

    @Override
    public void onReady(IPCClient client) {
        if (reconnecting) {
            getLogger().info("Discord RPC >> Reconnected!");
            reconnectTries = 1;
        } else {
            getLogger().info("Discord RPC >> Connected!");
            connectedBefore = true;
        }
        this.ipcClient = client;
        if (!ready) {
            ready = true;
        }
        if (lastRichPresence == null) {
            RichPresence.Builder builder = new RichPresence.Builder();
            lastRichPresence = builder.build();
        }
        client.sendRichPresence(lastRichPresence);
    }

    @Override
    public void onDisconnect(IPCClient client, Throwable t) {
        if (ready) {
            ready = false;
        }
        getLogger().info("Discord RPC >> Disconnected!");
    }

    @Override
    public void run() {
        while (autoReconnect) {
            try {
                // Connect if the connect on init didn't connect successfully
                if (!connectedBefore && !ready) {
                    lastPing = 0;
                    try {
                        ipcClient.connect();
                    } catch (Exception ex) { }
                    Thread.sleep(5000);
                    continue;
                }


                // Ping to make sure that the RPC is alive
                if (ready) {
                    Thread.sleep(1);
                    lastPing += 1;
                    if (lastPing >= 5 * 1000) {
                        sendRichPresence(this.lastRichPresence);
                        this.lastPing = 0;
                    }
                } else { // Reconnect to RPC with tries ( min: 2 seconds, max: 30 seconds )
                    lastPing = 0;
                    reconnecting = true;
                    int timeout = (reconnectTries * 2) * 1000;
                    getLogger().info("Discord RPC >> Reconnecting... (Timeout: " + timeout + "ms)");
                    try {
                        ipcClient.connect();
                    } catch (Exception ex) {
                        if (reconnectTries <= 15) {
                            reconnectTries += 1;
                        }
                        Thread.sleep(timeout);
                    }
                }
            } catch (Exception ex) { }
        }
    }

    @Override
    public void initialise(GameEngine engine, Context rootContext) {
        try {
            getLogger().info("Discord RPC >> Connecting...");
            ipcClient.connect();
        } catch (Exception ex) { }
    }

    @Override
    public void postInitialise(Context context) {
        config = context.get(Config.class);
        setState("In Lobby");
    }

    @Override
    public void preShutdown() {
        autoReconnect = false;
        reconnectThread.interrupt();
        ipcClient.close();
    }

    @Override
    public String getName() {
        return "DiscordRPC";
    }

    public static DiscordRPCSubSystem getInstance() {
        return instance;
    }

    public static Logger getLogger() {
        if (getInstance() == null) {
            return null;
        }
        return getInstance().logger;
    }

    public static void setState(String state) {
        setState(state, true);
    }

    public static void setState(String state, boolean timestamp) {
        if (instance == null) {
            return;
        }
        RichPresence.Builder builder = new RichPresence.Builder();
        if (state != null) {
            builder.setState(state);
            if (getInstance() == null || (getInstance().lastState != null && !getInstance().lastState.equals(state))) {
                getInstance().lastState = state;
            }
        }
        if (getInstance().config != null) {
            String playerName = getInstance().config.getPlayer().getName();
            builder.setDetails("IGN | " + playerName);
        }
        if (timestamp) {
            builder.setStartTimestamp(OffsetDateTime.now());
        }
        getInstance().sendRichPresence(builder.build());
    }

    public static void updateState() {
        if (getInstance() == null) {
            return;
        }
        setState(getInstance().lastState);
    }

}

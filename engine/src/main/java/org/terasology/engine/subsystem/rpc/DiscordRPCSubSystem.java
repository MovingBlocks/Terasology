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
import org.terasology.context.Context;
import org.terasology.engine.GameEngine;
import org.terasology.engine.subsystem.EngineSubsystem;

public class DiscordRPCSubSystem implements EngineSubsystem, IPCListener, Runnable {

    private static DiscordRPCSubSystem instance;

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
        if (instance == null) {
            return;
        }
        if (!getInstance().ready) {
            return;
        }
        RichPresence.Builder builder = new RichPresence.Builder();
        builder.setState(state);
        getInstance().sendRichPresence(builder.build());
    }

    private Logger logger;
    private IPCClient ipcClient;
    private boolean ready;
    private boolean autoReconnect;
    private Thread reconnectThread;
    private RichPresence lastRichPresence;
    private boolean reconnecting;
    private int reconnectTries;
    private boolean connectedBefore;

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
        if (!ready) {
            return;
        }
        ipcClient.sendRichPresence(lastRichPresence);
    }

    @Override
    public void onReady(IPCClient client) {
        if (reconnecting) {
            getLogger().info("Discord RPC >> Reconnected!");
            reconnectTries = 0;
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
            client.sendRichPresence(builder.build());
        } else {
            client.sendRichPresence(lastRichPresence);
        }
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
                if (!connectedBefore) {
                    ipcClient.connect();
                    Thread.sleep(5000);
                }
                if (ready) {
                    Thread.sleep(1);
                } else {
                    reconnecting = true;
                    getLogger().info("Discord RPC >> Reconnecting...");
                    ipcClient.connect();
                    int timeout = (reconnectTries * 5) * 1000;
                    Thread.sleep(timeout);
                    if (!ready) {
                        getLogger().info(String.format("Discord RPC >> Failed to reconnect! Retrying in %s seconds...", (timeout / 1000)));
                        if (reconnectTries <= 3) reconnectTries += 1;
                    }
                }
            } catch (Exception ex) {
                if (ex instanceof IllegalStateException) {
                    continue;
                }
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void initialise(GameEngine engine, Context rootContext) {
        try {
            getLogger().info("Discord RPC >> Connecting...");
            ipcClient.connect();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void postInitialise(Context context) {
        setState("In Lobby");
    }

    @Override
    public void preShutdown() {
        reconnectThread.interrupt();
        ipcClient.close();
    }

    @Override
    public String getName() {
        return "DiscordRPC";
    }
}

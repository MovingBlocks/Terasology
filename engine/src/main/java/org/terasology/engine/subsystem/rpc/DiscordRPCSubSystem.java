// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.subsystem.rpc;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import com.jagrosh.discordipc.IPCClient;
import com.jagrosh.discordipc.IPCListener;
import com.jagrosh.discordipc.entities.RichPresence;
import com.jagrosh.discordipc.entities.pipe.Pipe;
import com.jagrosh.discordipc.entities.pipe.WindowsPipe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.config.Config;
import org.terasology.context.Context;
import org.terasology.engine.GameEngine;
import org.terasology.engine.subsystem.EngineSubsystem;
import org.terasology.registry.In;

import java.time.OffsetDateTime;

/**
 * Subsystem that manages Discord RPC in the game client, such as status or connection.
 * This subsystem can be enhanced further to improve game presentation in rich presence.
 *
 * @see EngineSubsystem
 */
public class DiscordRPCSubSystem implements EngineSubsystem, IPCListener, Runnable {

    private static final Logger logger = LoggerFactory.getLogger(DiscordRPCSubSystem.class);
    private static final long DISCORD_APP_CLIENT_ID = 515274721080639504L;
    private static final String DISCORD_APP_LARGE_IMAGE = "ss_6";
    private static final int RECONNECT_TRIES = 5;
    private static DiscordRPCSubSystem instance;

    @In
    private Config config;

    private IPCClient ipcClient;
    private boolean ready;
    private boolean autoReconnect;
    private final Thread reconnectThread;
    private RichPresence lastRichPresence;
    private boolean reconnecting;
    private int reconnectTries = 1;
    private boolean connectedBefore;
    private String lastState;
    private boolean dontTryAgain;
    private boolean enabled;

    public DiscordRPCSubSystem() throws IllegalStateException {
        if (instance != null) {
            throw new IllegalStateException("More then one instance in the DiscordRPC");
        }
        lastRichPresence = null;
        ipcClient = new IPCClient(DISCORD_APP_CLIENT_ID);
        ipcClient.setListener(this);
        autoReconnect = true;
        reconnectThread = new Thread(this);
        reconnectThread.setName("DISCORD-RPC-RECONNECT");
        reconnectThread.start();
        instance = this;
        enabled = false;
        dontTryAgain = true;
    }

    public void sendRichPresence(RichPresence richPresence) {
        this.lastRichPresence = richPresence;
        if (!ready || lastRichPresence == null || !enabled) {
            return;
        }
        ipcClient.sendRichPresence(lastRichPresence);
    }

    @Override
    public void onReady(IPCClient client) {
        if (reconnecting) {
            logger.info("Discord RPC >> Reconnected!");
            reconnectTries = 1;
        } else {
            logger.info("Discord RPC >> Connected!");
            connectedBefore = true;
        }
        this.ipcClient = client;
        if (!ready) {
            ready = true;
        }
        if (lastRichPresence == null) {
            RichPresence.Builder builder = new RichPresence.Builder();
            builder.setLargeImage(DISCORD_APP_LARGE_IMAGE);
            lastRichPresence = builder.build();
        }
        client.sendRichPresence(lastRichPresence);
    }

    @Override
    public void onDisconnect(IPCClient client, Throwable t) {
        if (ready) {
            ready = false;
        }
        logger.info("Discord RPC >> Disconnected!");
    }

    @Override
    public void run() {
        while (autoReconnect) {
            try {
                // Ignore if the Discord RPC is not enabled
                if (!enabled) {
                    if (ready) {
                        getInstance().ipcClient.close();
                    }
                    Thread.sleep(1000);
                    continue;
                }

                // Don't retry to do any connect to the RPC till something happen to do it
                if (dontTryAgain) {
                    Thread.sleep(1000);
                    continue;
                }

                // Connect if the connect on init didn't connect successfully
                if (!connectedBefore && !ready) {
                    try {
                        ipcClient.connect();
                    } catch (Exception ex) {
                    } // Ignore the not able to connect to continue our process
                    Thread.sleep(15 * 1000);
                    if (!ready) {
                        reconnectTries += 1;
                        if (reconnectTries >= RECONNECT_TRIES) {
                            dontTryAgain = true;
                        }
                    }
                    continue;
                }

                // Ping to make sure that the RPC is alive
                if (ready) {
                    Thread.sleep(5000);
                    ipcClient.sendRichPresence(this.lastRichPresence);
                } else {
                    reconnecting = true;
                    int timeout = (reconnectTries * 2) * 1000;
                    logger.info("Discord RPC >> Reconnecting... (Timeout: " + timeout + "ms)");
                    try {
                        ipcClient.connect();
                    } catch (Exception ex) {
                        if (reconnectTries <= RECONNECT_TRIES) {
                            reconnectTries += 1;
                        }
                        if (reconnectTries >= RECONNECT_TRIES) {
                            dontTryAgain = true;
                        }
                        Thread.sleep(timeout);
                    }
                }
            } catch (InterruptedException ex) { // Ignore the interrupted exceptions
            } catch (Exception ex) {
                logger.trace(ex.getMessage(), ex.getCause());
            }
        }
    }

    @Override
    public void initialise(GameEngine engine, Context rootContext) {
        disableLogger(IPCClient.class);
        disableLogger(WindowsPipe.class);
        disableLogger(Pipe.class);
        enabled = config.getPlayer().isDiscordPresence();
        if (!enabled) {
            return;
        }
        try {
            logger.info("Discord RPC >> Connecting...");
            ipcClient.connect();
            dontTryAgain = false;
        } catch (Exception ex) { } // Ignore due to reconnect thread
    }

    @Override
    public void postInitialise(Context context) {
        setState("In Lobby");
    }

    @Override
    public void preShutdown() {
        autoReconnect = false;
        reconnectThread.interrupt();
        if (ready) {
          ipcClient.close();
        }
    }

    /**
     * To disable the logger from some classes that throw errors and some other spam stuff into our console.
     *
     */
    private void disableLogger(Class<?> clazz) {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger l = loggerContext.getLogger(clazz);
        ((ch.qos.logback.classic.Logger) l).setLevel(Level.OFF);
    }

    @Override
    public String getName() {
        return "DiscordRPC";
    }

    public static DiscordRPCSubSystem getInstance() {
        return instance;
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
            if (getInstance().lastState == null || (getInstance().lastState != null && !getInstance().lastState.equals(state))) {
                getInstance().lastState = state;
            }
        }
        if (getInstance().config != null) {
            String playerName = getInstance().config.getPlayer().getName();
            builder.setDetails("Name: " + playerName);
        }
        if (timestamp) {
            builder.setStartTimestamp(OffsetDateTime.now());
        }

        builder.setLargeImage(DISCORD_APP_LARGE_IMAGE);
        getInstance().sendRichPresence(builder.build());
    }

    public static void updateState() {
        if (getInstance() == null) {
            return;
        }
        setState(getInstance().lastState);
    }

    public static void tryToDiscover() {
        if (getInstance() == null) {
            return;
        }
        if (getInstance().dontTryAgain && getInstance().enabled) {
            getInstance().dontTryAgain = false;
            getInstance().reconnectTries = 0;
        }
    }

    public static void enable() {
        setEnabled(true);
    }

    public static void disable() {
        setEnabled(false);
    }

    public static void setEnabled(boolean enable) {
        if (getInstance() == null) {
            return;
        }
        getInstance().enabled = enable;
        if (!enable) {
            getInstance().reconnectTries = 0;
        } else {
            tryToDiscover();
        }
    }

    public static boolean isEnabled() {
        if (getInstance() == null) {
            return false;
        }
        return getInstance().enabled;
    }

}

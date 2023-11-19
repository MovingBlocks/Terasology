// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.subsystem.discordrpc;

import com.jagrosh.discordipc.IPCClient;
import com.jagrosh.discordipc.IPCListener;
import com.jagrosh.discordipc.entities.RichPresence;
import com.jagrosh.discordipc.exceptions.NoDiscordClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DiscordRPCThread implements IPCListener, Runnable {
    private static final Logger logger = LoggerFactory.getLogger(DiscordRPCThread.class);
    private static final long DISCORD_APP_CLIENT_ID = 515274721080639504L;
    private static final String DISCORD_APP_DEFAULT_IMAGE = "ss_6";
    private static final int MAX_RECONNECT_TRIES = 5;

    private final Thread thread;
    private final IPCClient ipcClient;
    private final DiscordRPCBuffer buffer;
    private RichPresence lastRichPresence;

    private int tries;

    private boolean enabled;
    private boolean waiting;
    private boolean connectedBefore;
    private boolean connected;
    private boolean autoReconnect;

    public DiscordRPCThread() {
        thread = new Thread(this);
        thread.setName("DISCORD-RPC-THREAD");

        ipcClient = new IPCClient(DISCORD_APP_CLIENT_ID);
        ipcClient.setListener(this);

        buffer = new DiscordRPCBuffer();

        lastRichPresence = null;

        tries = 0;

        enabled = false;
        waiting = false;
        connectedBefore = false;
        connected = false;
        autoReconnect = false;
    }

    public void start() {
        thread.start();
    }

    public synchronized void stop() {
        synchronized (thread) {
            thread.interrupt();
        }
    }

    public synchronized void discover() {
        if (enabled && connected) {
            return;
        }

        reset(true);

        connectedBefore = true;
    }

    public synchronized void enable() {
        if (enabled) {
            return;
        }

        enabled = true;
        autoReconnect = true;

        if (waiting && thread.isAlive()) {
            synchronized (thread) {
                thread.notify();
            }
        }
    }

    public synchronized void disable() {
        disable(false);
    }

    public synchronized void disable(boolean keepConnectionAlive) {
        if (!enabled) {
            return;
        }

        enabled = false;

        reset(!keepConnectionAlive);
        if (!keepConnectionAlive) {
            autoReconnect = false;
        }

        if (waiting && thread.isAlive()) {
            synchronized (thread) {
                thread.notify();
            }
        }
    }

    @Override
    public void onReady(IPCClient ignored) {
        if (connectedBefore) {
            logger.info("Re-connected to Discord RPC!");
        } else {
            logger.info("Connected to Discord RPC!");
        }

        connectedBefore = true;
        connected = true;
    }

    @Override
    public void onDisconnect(IPCClient client, Throwable t) {
        connected = false;
        logger.info("Discord RPC lost connection: Disconnected!");
    }

    @Override
    public void run() {
        while (true) {
            logger.info("Waiting for auto-connecting...");
            /* If auto-connect is disabled the thread won't get notified*/
            while (!autoReconnect) {
                try {
                    synchronized (thread) {
                        waiting = true;
                        thread.wait();
                        waiting = false;
                    }
                } catch (InterruptedException ignored) {
                    return; // End when the thread is being interrupted
                }
            }

            logger.info("Waiting for enabling...");
            /* Check if the subsystem is enabled */
            while (!enabled) {
                try {
                    synchronized (thread) {
                        waiting = true;
                        thread.wait();
                        waiting = false;
                    }
                } catch (InterruptedException ignored) {
                    return; // End when the thread is being interrupted
                }
            }

            logger.info("Waiting for connection...");
            /* Auto-Connect to the IPC with reconnect process */
            while (!connected) {
                synchronized (ipcClient) {
                    try {
                        if (!connectedBefore) {
                            logger.info("Connecting to Discord RPC...");
                        } else {
                            logger.info("Re-connecting to Discord RPC...");
                        }

                        ipcClient.connect();

                        tries = 0;
                        autoReconnect = true;
                    } catch (NoDiscordClientException ignored) {
                        // TODO implement reconnect process
                        if (tries >= MAX_RECONNECT_TRIES) {
                            autoReconnect = false;
                            tries = 0;
                            break;
                        } else {
                            tries++;
                            try {
                                Thread.sleep(2000L * tries);
                            } catch (InterruptedException ignored2) {
                                ipcClient.close();
                                return; // End when the thread is being interrupted
                            }

                            // Retry to connect again
                        }
                    } catch (RuntimeException e) {
                        logger.error("Could not create or connect Discord client: ", e);
                        return;
                    }
                }
            }

            /* Go to the beginning to trigger auto reconnect loop */
            if (!autoReconnect) {
                continue;
            }

            logger.info("Updating the rich presence and keep the connection alive...");
            /* Update the rich presence and keeping the connection alive */
            while (connected) {
                synchronized (this) {
                    /* Ping the ipc connection with a rich presence to keep the connection alive */
                    if (enabled) {
                        /* Allocate a new rich presence when the buffer has changed */
                        if (buffer.hasChanged() && buffer.isEmpty()) {
                            lastRichPresence = null;
                            buffer.resetState();
                        } else if (buffer.hasChanged()) {
                            lastRichPresence = build();
                            buffer.resetState();
                        }

                        ipcClient.sendRichPresence(lastRichPresence);
                    } else {
                        ipcClient.sendRichPresence(null);
                    }
                }

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ignored) {
                    synchronized (ipcClient) {
                        ipcClient.close();
                    }
                    return;
                }
            }
        }
    }

    public synchronized void setEnabled(boolean enabled) {
        if (this.enabled != enabled) {
            if (enabled) {
                enable();
            } else {
                disable(true);
            }
        }

        this.enabled = enabled;
    }

    public synchronized boolean isEnabled() {
        return enabled;
    }

    public synchronized DiscordRPCBuffer getBuffer() {
        return buffer;
    }

    private RichPresence build() {
        RichPresence.Builder builder = new RichPresence.Builder()
                .setLargeImage(DISCORD_APP_DEFAULT_IMAGE);

        if (buffer.getDetails() != null) {
            builder.setDetails(buffer.getDetails());
        }

        if (buffer.getState() != null) {
            builder.setState(buffer.getState());
        }

        if (buffer.getStartTimestamp() != null) {
            builder.setStartTimestamp(buffer.getStartTimestamp());
        }

        int partySize = buffer.getPartySize();
        int partyMax = buffer.getPartyMax();
        if (partySize > 0 && partyMax > 0) {
            builder.setParty("null", partySize, partyMax);
        }

        return builder.build();
    }

    private void reset(boolean resetConnection) {
        tries = 0;

        autoReconnect = true;
        if (resetConnection) {
            connectedBefore = false;
            connected = false;
        }
    }
}

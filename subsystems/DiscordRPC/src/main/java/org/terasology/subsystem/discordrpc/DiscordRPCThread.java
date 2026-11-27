// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.subsystem.discordrpc;

import com.jagrosh.discordipc.IPCClient;
import com.jagrosh.discordipc.IPCListener;
import com.jagrosh.discordipc.entities.RichPresence;
import com.jagrosh.discordipc.entities.pipe.PipeStatus;
import com.jagrosh.discordipc.exceptions.NoDiscordClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.function.BooleanSupplier;

public final class DiscordRPCThread implements IPCListener, Runnable {
    static final int MAX_RECONNECT_TRIES = 5;
    private static final Logger logger = LoggerFactory.getLogger(DiscordRPCThread.class);
    private static final long DISCORD_APP_CLIENT_ID = 515274721080639504L;
    private static final String DISCORD_APP_DEFAULT_IMAGE = "ss_6";
    private static final long DEFAULT_RETRY_BASE_DELAY_MS = 2000L;
    private static final long PRESENCE_UPDATE_INTERVAL_MS = 5000L;

    private final Thread thread;
    private final IPCClient ipcClient;
    private final DiscordRPCBuffer buffer;
    private final long retryBaseDelayMs;
    private RichPresence lastRichPresence;

    private int tries;

    private volatile boolean enabled;
    private volatile boolean waiting;
    private volatile boolean connectedBefore;
    private volatile boolean connected;
    private volatile boolean autoReconnect;

    public DiscordRPCThread() {
        this(new IPCClient(DISCORD_APP_CLIENT_ID), DEFAULT_RETRY_BASE_DELAY_MS);
    }

    DiscordRPCThread(IPCClient ipcClient, long retryBaseDelayMs) {
        thread = new Thread(this);
        thread.setName("DISCORD-RPC-THREAD");

        this.ipcClient = ipcClient;
        this.ipcClient.setListener(this);
        this.retryBaseDelayMs = retryBaseDelayMs;

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
        wakeThread();
    }

    public synchronized void enable() {
        if (enabled) {
            return;
        }

        enabled = true;
        autoReconnect = true;
        wakeThread();
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

        wakeThread();
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
            /* If auto-connect is disabled the thread won't get notified */
            if (!awaitCondition(() -> autoReconnect, "Waiting for auto-connecting...")) {
                return;
            }

            /* Check if the subsystem is enabled */
            if (!awaitCondition(() -> enabled, "Waiting for enabling...")) {
                return;
            }

            logger.info("Waiting for connection...");
            /* Auto-Connect to the IPC with reconnect process */
            while (!connected && enabled && autoReconnect) {
                synchronized (ipcClient) {
                    try {
                        if (tries == 0) {
                            if (!connectedBefore) {
                                logger.info("Connecting to Discord RPC...");
                            } else {
                                logger.info("Re-connecting to Discord RPC...");
                            }
                        } else if (logger.isDebugEnabled()) {
                            logger.debug("Retrying Discord RPC connection (attempt {}/{})...",
                                    tries + 1, MAX_RECONNECT_TRIES + 1);
                        }

                        ipcClient.connect();
                        if (Thread.currentThread().isInterrupted()) {
                            closeClientIfConnected();
                            return;
                        }

                        tries = 0;
                        autoReconnect = true;
                    } catch (NoDiscordClientException e) {
                        if (!handleUnavailableClient(e)) {
                            return;
                        }
                    } catch (RuntimeException e) {
                        if (isIpcConnectionFailure(e)) {
                            // DiscordIPC 0.4 wraps Unix/Windows pipe IOExceptions (e.g. SocketException ENOENT)
                            // in RuntimeException instead of letting Pipe.openPipe throw NoDiscordClientException.
                            if (!handleUnavailableClient(e)) {
                                return;
                            }
                        } else {
                            logger.error("Could not create or connect Discord client: ", e);
                            return;
                        }
                    }
                }
            }

            /* Go to the beginning to trigger auto reconnect loop */
            if (!autoReconnect || !enabled) {
                continue;
            }

            logger.info("Updating the rich presence and keep the connection alive...");
            /* Update the rich presence and keeping the connection alive */
            while (connected) {
                synchronized (this) {
                    if (!connected) {
                        break;
                    }
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

                        sendRichPresenceSafely(lastRichPresence);
                    } else {
                        sendRichPresenceSafely(null);
                    }
                }

                try {
                    Thread.sleep(PRESENCE_UPDATE_INTERVAL_MS);
                } catch (InterruptedException ignored) {
                    closeClientIfConnected();
                    return;
                }
            }
        }
    }

    boolean isConnected() {
        return connected;
    }

    boolean isAutoReconnect() {
        return autoReconnect;
    }

    boolean isWaiting() {
        return waiting;
    }

    boolean isThreadAlive() {
        return thread.isAlive();
    }

    void joinThread(long timeoutMs) throws InterruptedException {
        thread.join(timeoutMs);
    }

    Thread.State getThreadState() {
        return thread.getState();
    }

    private boolean awaitCondition(BooleanSupplier condition, String waitMessage) {
        if (condition.getAsBoolean()) {
            return true;
        }
        logger.info(waitMessage);
        while (!condition.getAsBoolean()) {
            try {
                synchronized (thread) {
                    if (!condition.getAsBoolean()) {
                        waiting = true;
                        try {
                            thread.wait();
                        } finally {
                            waiting = false;
                        }
                    }
                }
            } catch (InterruptedException ignored) {
                closeClientIfConnected();
                return false; // End when the thread is being interrupted
            }
        }
        return true;
    }

    private void wakeThread() {
        synchronized (thread) {
            if (waiting && thread.isAlive()) {
                thread.notifyAll();
            }
        }
    }

    private boolean handleUnavailableClient(Exception cause) {
        if (tries >= MAX_RECONNECT_TRIES) {
            if (logger.isInfoEnabled()) {
                logger.info("Discord client unavailable after {} attempts ({}); "
                        + "Discord Rich Presence will stay inactive until next discovery.",
                        MAX_RECONNECT_TRIES + 1, cause.getMessage());
            }
            autoReconnect = false;
            tries = 0;
            return true;
        }
        tries++;
        try {
            Thread.sleep(retryBaseDelayMs * tries);
        } catch (InterruptedException ignored) {
            closeClientIfConnected();
            return false; // End when the thread is being interrupted
        }
        return true;
    }

    private static boolean isIpcConnectionFailure(Throwable throwable) {
        Throwable current = throwable.getCause();
        while (current != null) {
            if (current instanceof IOException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private void sendRichPresenceSafely(RichPresence presence) {
        synchronized (ipcClient) {
            if (ipcClient.getStatus() == PipeStatus.CONNECTED) {
                try {
                    ipcClient.sendRichPresence(presence);
                } catch (IllegalStateException e) {
                    logger.debug("Discord IPC disconnected before Rich Presence could be sent", e);
                    connected = false;
                }
            } else {
                connected = false;
            }
        }
    }

    private void closeClientIfConnected() {
        synchronized (ipcClient) {
            if (ipcClient.getStatus() == PipeStatus.CONNECTED) {
                try {
                    ipcClient.close();
                } catch (IllegalStateException e) {
                    logger.debug("Discord IPC already disconnected before close", e);
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

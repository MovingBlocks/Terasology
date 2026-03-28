// Copyright 2026 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.subsystem.nakama;

/**
 * Configuration for the Nakama subsystem.
 * Read from system properties for the POC.
 */
public class NakamaConfig {
    private boolean enabled = false;
    private String host = "localhost";
    private int grpcPort = 7349;
    private int wsPort = 7350;
    private String channel = "bifrost.lobby";
    private String playerName = "";

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }

    /** gRPC port for API calls (auth, account). Default: 7349. */
    public int getGrpcPort() { return grpcPort; }
    public void setGrpcPort(int grpcPort) { this.grpcPort = grpcPort; }

    /** @deprecated Use {@link #getGrpcPort()}. Kept for backwards compatibility. */
    @Deprecated
    public int getPort() { return grpcPort; }
    /** @deprecated Use {@link #setGrpcPort(int)}. */
    @Deprecated
    public void setPort(int port) { this.grpcPort = port; }

    /** WebSocket port for realtime (chat, presence). Default: 7350. */
    public int getWsPort() { return wsPort; }
    public void setWsPort(int wsPort) { this.wsPort = wsPort; }

    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }

    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }

    /**
     * Load config from system properties (nakama.enabled, nakama.host, etc.)
     * Falls back to defaults if not set.
     */
    public static NakamaConfig fromSystemProperties() {
        NakamaConfig config = new NakamaConfig();
        config.setEnabled(Boolean.parseBoolean(System.getProperty("nakama.enabled", "false")));
        config.setHost(System.getProperty("nakama.host", "localhost"));
        config.setGrpcPort(Integer.parseInt(System.getProperty("nakama.grpcPort", "7349")));
        config.setWsPort(Integer.parseInt(System.getProperty("nakama.wsPort", "7350")));
        config.setChannel(System.getProperty("nakama.channel", "bifrost.lobby"));
        config.setPlayerName(System.getProperty("nakama.playerName", ""));
        return config;
    }
}

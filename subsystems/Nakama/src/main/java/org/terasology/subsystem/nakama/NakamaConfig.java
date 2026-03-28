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
    private int port = 7349;
    private String channel = "bifrost.lobby";
    private String playerName = "";

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }

    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }

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
        config.setPort(Integer.parseInt(System.getProperty("nakama.port", "7349")));
        config.setChannel(System.getProperty("nakama.channel", "bifrost.lobby"));
        config.setPlayerName(System.getProperty("nakama.playerName", ""));
        return config;
    }
}

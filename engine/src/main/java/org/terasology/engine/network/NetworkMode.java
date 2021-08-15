// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.network;

/**
 * The current multiplayer mode of the game.
 *
 */
public enum NetworkMode {
    /**
     * The game is running in single-player mode.
     */
    NONE(true, false, true),

    /**
     * The game is hosting a server without local player
     */
    LISTEN_SERVER(true, true, false),
    
    /**
     * The game is hosting a server with local player
     */
    DEDICATED_SERVER(true, true, true),
    
    /**
     * The game is a remote client connected to a server
     */
    CLIENT(false, false, true);

    private boolean authority;
    private boolean isServer;
    private boolean hasLocalClient;

    NetworkMode(boolean authority, boolean isServer, boolean hasLocalClient) {
        this.authority = authority;
        this.isServer = isServer;
        this.hasLocalClient = hasLocalClient;
    }

    /**
     * @return Whether the game is the authority on what is happening in the world
     */
    public boolean isAuthority() {
        return authority;
    }
    
    /**
     * @return true if the game is hosting a server
     */
    public boolean isServer() {
        return isServer;
    }
    
    /**
     * @return true if the instance has a local client
     */
    public boolean hasLocalClient() {
        return hasLocalClient;
    }
    
}

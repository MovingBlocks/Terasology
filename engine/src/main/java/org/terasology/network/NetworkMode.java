/*
 * Copyright 2013 MovingBlocks
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

package org.terasology.network;

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

    private NetworkMode(boolean authority, boolean isServer, boolean hasLocalClient) {
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

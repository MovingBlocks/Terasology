/*
 * Copyright 2013 Moving Blocks
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
 * @author Immortius
 */
public enum NetworkMode {
    /**
     * The game is running in single-player mode.
     */
    NONE(true),

    /**
     * The game is hosting a server.
     */
    SERVER(true),

    /**
     * The game is a remote client connected to a server
     */
    CLIENT(false);

    private boolean authority;

    private NetworkMode(boolean authority) {
        this.authority = authority;
    }

    /**
     * @return Whether the game is the authority on what is happening in the world
     */
    public boolean isAuthority() {
        return authority;
    }
}

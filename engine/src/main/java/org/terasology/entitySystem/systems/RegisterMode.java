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

package org.terasology.entitySystem.systems;

import org.terasology.network.NetworkMode;

/**
 * Enumeration of registration modes for ComponentSystems.
 * The registration mode dictates if a component system should be run locally or not.
 *
 */
public enum RegisterMode {
    /**
     * Always
     */
    ALWAYS(true, true, true),
    /**
     * Only if the application is acting as the authority (single player, listen or dedicated server)
     */
    AUTHORITY(true, false, true),
    /**
     * Only if the application is hosting a player (single player, remote client or listen server)
     */
    CLIENT(true, true, false),
    /**
     * Only if the application is a remote client.
     */
    REMOTE_CLIENT(false, true, false);


    private boolean validWhenAuthority;
    private boolean validWhenRemote;
    private boolean validWhenHeadless;

    private RegisterMode(boolean validWhenAuthority, boolean validWhenRemote, boolean validWhenHeadless) {
        this.validWhenAuthority = validWhenAuthority;
        this.validWhenRemote = validWhenRemote;
        this.validWhenHeadless = validWhenHeadless;
    }

    public boolean isValidFor(NetworkMode mode, boolean headless) {
        return ((mode.isAuthority()) ? validWhenAuthority : validWhenRemote) && (!headless || validWhenHeadless);
    }
}

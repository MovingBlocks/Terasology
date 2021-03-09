// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.entitySystem.systems;

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

     RegisterMode(boolean validWhenAuthority, boolean validWhenRemote, boolean validWhenHeadless) {
        this.validWhenAuthority = validWhenAuthority;
        this.validWhenRemote = validWhenRemote;
        this.validWhenHeadless = validWhenHeadless;
    }

    public boolean isValidFor(boolean isAuthority, boolean headless) {
        return (isAuthority ? validWhenAuthority : validWhenRemote) && (!headless || validWhenHeadless);
    }
}

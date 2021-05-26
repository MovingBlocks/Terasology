// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.subsystem.discordrpc;

import java.time.OffsetDateTime;

/**
 * A threaded-safe shared buffer used to store information for {@link DiscordRPCThread} to be processed as {@link com.jagrosh.discordipc.entities.RichPresence}
 *
 * It helps avoiding allocating unnecessary objects for the rich presence.
 */
public final class DiscordRPCBuffer {
    private String details;
    private String state;
    private OffsetDateTime startTimestamp;
    private int partySize;
    private int partyMax;
    private boolean changed;

    public DiscordRPCBuffer() {
        reset();
    }

    /**
     * Resets the buffer data
     */
    public synchronized void reset() {
        this.details = null;
        this.state = null;
        this.startTimestamp = null;
        this.changed = true;
        this.partySize = -1;
        this.partyMax = -1;
    }

    /**
     * Sets the details of the current game
     *
     * @param details Details about the current game (null for nothing)
     */
    public synchronized void setDetails(String details) {
        this.details = details;
    }

    /**
     * Gets the details about the current game
     *
     * @return Detail about the current game
     */
    public synchronized String getDetails() {
        return details;
    }

    /**
     * Sets the current party status
     *
     * @param state The current party status (null for nothing)
     */
    public synchronized void setState(String state) {
        this.state = state;
        this.changed = true;
    }

    /**
     * Returns the current party status
     *
     * @return The current party status
     */
    public synchronized String getState() {
        return state;
    }

    /**
     * Sets the start of the game
     *
     * @param startTimestamp The time when that action has start or null to hide it
     */
    public synchronized void setStartTimestamp(OffsetDateTime startTimestamp) {
        this.startTimestamp = startTimestamp;
        this.changed = true;
    }

    /**
     * Returns the start of the game
     *
     * @return The start of the game
     */
    public synchronized OffsetDateTime getStartTimestamp() {
        return startTimestamp;
    }

    /**
     * Sets the current party size
     *
     * @param partySize The current party size
     */
    public synchronized void setPartySize(int partySize) {
        this.partySize = partySize;
        this.changed = true;
    }

    /**
     * Returns the current party size
     *
     * @return The party size
     */
    public synchronized int getPartySize() {
        return partySize;
    }

    /**
     * Sets the maximum number of the players in the party
     *
     * @param partyMax The number of the players
     */
    public synchronized void setPartyMax(int partyMax) {
        this.partyMax = partyMax;
        this.changed = true;
    }

    /**
     * Returns the maximum number of players in the party
     *
     * @return The maximum number of players in the party
     */
    public synchronized int getPartyMax() {
        return partyMax;
    }

    /**
     * Check if the buffer has changed
     *
     * @return if the buffer has changed
     */
    public synchronized boolean hasChanged() {
        return changed;
    }

    /**
     * Check if the buffer is empty
     *
     * @return if the buffer is empty
     */
    public synchronized boolean isEmpty() {
        return this.details == null && this.state == null && this.startTimestamp == null;
    }

    /**
     * Resets the buffer's change state to false
     */
    synchronized void resetState() {
        this.changed = false;
    }
}

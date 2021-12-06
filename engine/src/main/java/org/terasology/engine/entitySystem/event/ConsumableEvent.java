// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.entitySystem.event;

import org.terasology.gestalt.entitysystem.event.Event;

/**
 * A consumable event is an event that can be prevented from continuing through remaining event receivers. This is
 * primarily useful for input event.
 *
 */
public interface ConsumableEvent extends Event {

    /**
     * Tells whether or not the Event has been consumed.
     * @return true if the the event has been consumed, false otherwise.
     */
    boolean isConsumed();

    /**
     * Marks the Event as consumed.
     * Makes subsequent {@link #isConsumed()} calls return true.
     */
    void consume();
}

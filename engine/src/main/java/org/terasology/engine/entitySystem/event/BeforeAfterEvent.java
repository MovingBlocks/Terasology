// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.entitySystem.event;

import org.terasology.gestalt.entitysystem.event.Event;

/**
 * Immutable event to notify a change in a certain value.
 * <p>
 * Send this event to notify interested systems about a change in a value. It is good practice to only sent this event
 * if the {@code newValue} is different from the {@code oldValue}. The event should notify about a change that has
 * already happened. The event can neither be consumed nor modified.
 * <p>
 * <strong>Note:</strong> This is a generic event and a more specific Event extending this event should be fired and
 * reacted to.
 */
public abstract class BeforeAfterEvent<T> implements Event {
    protected final T oldValue;
    protected final T newValue;

    /**
     * Creates a new notification event on change in value.
     *
     * @param oldValue the value prior to the change, may be {@code null}
     * @param newValue the value after the the change, may be {@code null}
     */
    public BeforeAfterEvent(final T oldValue, final T newValue) {
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    /**
     * Returns the value prior to change.
     *
     * @return the value prior to change, may be {@code null}
     */
    public T getOldValue() {
        return oldValue;
    }

    /**
     * Returns the value after change.
     *
     * @return the value after the change, may be {@code null}
     */
    public T getNewValue() {
        return newValue;
    }
}

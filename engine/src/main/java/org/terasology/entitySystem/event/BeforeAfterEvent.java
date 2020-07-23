// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.entitySystem.event;

/**
 * Immutable event to notify a change in a certain value.
 * NOTE: This is a generic event and a more specific Event extending this event should be fired and reacted to.
 */
public abstract class BeforeAfterEvent<T> implements Event {
    protected final T oldValue;
    protected final T newValue;

    /**
     * Creates a new notification event on change in value.
     */
    BeforeAfterEvent(final T oldValue, final T newValue) {
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    /**
     * Returns the value prior to change.
     */
    public T getOldValue() {
        return oldValue;
    }

    /**
     * Returns the value after change.
     */
    public T getNewValue() {
        return newValue;
    }
}

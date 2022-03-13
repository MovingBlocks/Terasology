// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.entitySystem.entity.lifecycleEvents;

import org.terasology.gestalt.entitysystem.event.Event;

/**
 * This event is sent when an entity is created or a component added to an existing entity.
 * <br><br>
 * Note that this event will only be received by @ReceiveEvent methods where all components in its list are present and
 * at least one is involved in the action causing the event.
 *
 * @see org.terasology.engine.entitySystem.entity.lifecycleEvents.BeforeRemoveComponent
 */
public final class OnAddedComponent implements Event {

    private static OnAddedComponent instance = new OnAddedComponent();

    private OnAddedComponent() {
    }

    public static OnAddedComponent newInstance() {
        return instance;
    }
}

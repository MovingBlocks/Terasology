// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.entitySystem.entity.lifecycleEvents;

import org.terasology.gestalt.entitysystem.event.Event;

/**
 * When a component is about to be removed from an entity, or an entity is about to be destroyed, this event is sent.
 * <br><br>
 * Note that this event will only be received by @ReceiveEvent methods where all components in its list are present and
 * at least one is involved in the action causing the event.
 *
 * @see org.terasology.engine.entitySystem.entity.lifecycleEvents.OnAddedComponent
 */
public final class BeforeRemoveComponent implements Event {
    private static BeforeRemoveComponent instance = new BeforeRemoveComponent();

    private BeforeRemoveComponent() {
    }

    public static BeforeRemoveComponent newInstance() {
        return instance;
    }


}

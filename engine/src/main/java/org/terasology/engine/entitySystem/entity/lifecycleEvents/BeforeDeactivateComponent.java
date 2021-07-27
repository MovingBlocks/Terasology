// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.entitySystem.entity.lifecycleEvents;

import org.terasology.engine.entitySystem.event.Event;

/**
 * When a component is about to leave the active state, either due to being removed, the entity it is attached to being destroyed,
 * or the entity being stored, this event is sent.
 * <br><br>
 * Note that this event will only be received by @ReceiveEvent methods where all components in its list are present and
 * at least one is involved in the action causing the event.
 *
 */
public final class BeforeDeactivateComponent implements Event {

    private static BeforeDeactivateComponent instance = new BeforeDeactivateComponent();

    private BeforeDeactivateComponent() {
    }

    public static BeforeDeactivateComponent newInstance() {
        return instance;
    }

}

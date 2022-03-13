// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.entitySystem.entity.lifecycleEvents;

import org.terasology.gestalt.entitysystem.event.Event;

/**
 * This event occurs after an entity is created, an entity is loaded or a component is added to an entity. This occurs
 * after OnAddedComponent where relevant.
 * <br><br>
 * Note that this event will only be received by @ReceiveEvent methods where all components in its list are present and
 * at least one is involved in the action causing the event.
 *
 */
public final class OnActivatedComponent implements Event {

    private static OnActivatedComponent instance = new OnActivatedComponent();

    private OnActivatedComponent() {
    }

    public static OnActivatedComponent newInstance() {
        return instance;
    }

}

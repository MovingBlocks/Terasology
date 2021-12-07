// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.entitySystem.entity.lifecycleEvents;

import org.terasology.gestalt.entitysystem.event.Event;

/**
 * This event occurs whenever a component is changed and saved.
 * <br><br>
 * Note that this event will only be received by @ReceiveEvent methods where all components in its list are present and
 * at least one is involved in the action causing the event.
 *
 */
public final class OnChangedComponent implements Event {

    private static OnChangedComponent instance = new OnChangedComponent();

    private OnChangedComponent() {
    }

    public static OnChangedComponent newInstance() {
        return instance;
    }
}

// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui;

import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.engine.entitySystem.event.Event;
import org.terasology.gestalt.module.sandbox.API;
import org.terasology.engine.network.OwnerEvent;

/**
 * The event is sent to the UI layer
 *
 */
@OwnerEvent
@API
public class ScreenLayerClosedEvent implements Event {
    private ResourceUrn closedScreenUri;

    // Default constructor for serialization
    ScreenLayerClosedEvent() {
    }

    public ScreenLayerClosedEvent(ResourceUrn closedScreenUri) {
        this.closedScreenUri = closedScreenUri;
    }

    public ResourceUrn getClosedScreenUri() {
        return closedScreenUri;
    }
}

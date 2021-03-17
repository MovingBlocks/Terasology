// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.world.viewDistance;

import org.terasology.engine.entitySystem.event.Event;
import org.terasology.engine.network.ServerEvent;

/**
 * Notifies the server that the client's view distance has changed.
 * <p>
 * Sent from the local player's client entity to the server. This event only functions as notification for the server,
 * but not the other way around (e.g., for the server to set the view distance on clients).
 */
@ServerEvent
public class ViewDistanceChangedEvent implements Event {

    private ViewDistance newViewRange;

    protected ViewDistanceChangedEvent() {
    }

    /**
     * @param viewDistance The view range mode (not distance)
     */
    public ViewDistanceChangedEvent(ViewDistance viewDistance) {
        newViewRange = viewDistance;
    }

    public ViewDistance getNewViewRange() {
        return newViewRange;
    }
}

// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.world.viewDistance;

import org.terasology.engine.entitySystem.event.Event;
import org.terasology.engine.network.ServerEvent;

/**
 * Notifies the server that the clients view distance changed.
 *
 * If you want to change the view distance send {@link ViewDistanceChangeRequest} to the local player's client entity.
 *
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

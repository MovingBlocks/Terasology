/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.rendering.world.viewDistance;

import org.terasology.entitySystem.event.Event;
import org.terasology.network.ServerEvent;

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

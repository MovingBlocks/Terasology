/*
 * Copyright 2015 MovingBlocks
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
package org.terasology.logic.inventory.events;

import org.terasology.entitySystem.event.Event;
import org.terasology.math.JomlUtil;
import org.terasology.math.geom.Vector3f;
import org.terasology.network.ServerEvent;

/**
 * Fire this event on an item in order for the authority to add the necessary components to put it in the world.
 */
@ServerEvent
public class DropItemEvent implements Event {
    private Vector3f position;

    public DropItemEvent() {
    }

    /**
     * @deprecated This method is scheduled for removal in an upcoming version.
     *             Use the JOML implementation instead: {@link #DropItemEvent(org.joml.Vector3f)}.
     */
    public DropItemEvent(Vector3f position) {
        this.position = position;
    }

    public DropItemEvent(org.joml.Vector3f position) {
        this.position = JomlUtil.from(position);
    }

    public Vector3f getPosition() {
        return position;
    }
}

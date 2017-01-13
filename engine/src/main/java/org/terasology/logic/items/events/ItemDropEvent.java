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
package org.terasology.logic.items.events;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.Event;
import org.terasology.math.geom.Vector3f;
import org.terasology.network.ServerEvent;

/**
 * Event to be fired to drop an item into the world.
 */
@ServerEvent
public class ItemDropEvent implements Event {
    private Vector3f position;
    private EntityRef item;


    public ItemDropEvent(Vector3f position) {
        this(position, EntityRef.NULL);
    }

    public ItemDropEvent(Vector3f position, EntityRef item) {
        this.item = item;
        this.position = position;
    }

    public EntityRef getItem() {
        return item;
    }

    public Vector3f getPosition() {
        return position;
    }
}

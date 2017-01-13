/*
 * Copyright 2016 MovingBlocks
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
import org.terasology.network.ServerEvent;

/**
 * Event to be sent when an item should be added to an inventory
 */
@ServerEvent
public class ItemGiveEvent implements Event {
    private EntityRef destInv = EntityRef.NULL;
    private EntityRef item = EntityRef.NULL;

    private boolean success;
    private int count = 1;

    public ItemGiveEvent(EntityRef dest) {
        this(dest, EntityRef.NULL, 1);
    }

    public ItemGiveEvent(EntityRef dest, EntityRef item) {
        this(dest, item, 1);
    }

    /**
     * @param dest  The destination inventory.
     * @param item  The item to give inventory.
     * @param count The number of items to give.
     */
    public ItemGiveEvent(EntityRef dest, EntityRef item, int count) {
        destInv = dest;
        this.item = item;
        this.count = count;
    }

    /**
     * @param count The new number of items to give
     */
    public void setCount(int count) {
        this.count = count;
    }

    /**
     * @return How many items to give
     */
    public int getCount() {
        return count;
    }

    /**
     * @param item The item to give
     */
    public void setItem(EntityRef item) {
        this.item = item == null ? EntityRef.NULL : item;
    }

    /**
     * @return The item to give
     */
    public EntityRef getItem() {
        return item;
    }

    /**
     * @return The destination inventory
     */
    public EntityRef getDestInv() {
        return destInv;
    }

    /**
     * @param value A boolean indicating the success of giving the item.
     */
    public void setSuccess(boolean value) {
        success = value;
    }

    /**
     * @return True if the event was successful in giving the item.
     */
    public boolean wasSuccessful() {
        return success;
    }

}

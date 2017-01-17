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
import org.terasology.module.sandbox.API;
import org.terasology.network.ServerEvent;

/**
 * Event to be fired when an item should be moved from one inventory to another.
 */
@ServerEvent
@API
public class ItemMoveEvent implements Event {
    private EntityRef destInv = EntityRef.NULL;
    private EntityRef sourceInv = EntityRef.NULL;
    private EntityRef item = EntityRef.NULL;

    private boolean success;
    private int count = 1;

    private Class component;
    /**
     * 0 = Move the first available item to the first valid slot
     * 1 = Move the selected item to the first valid slot
     * 2 = Move the first item with component to the first valid slot
     */
    private int moveType = 0;

    public ItemMoveEvent() {
    }

    public ItemMoveEvent(EntityRef source, EntityRef dest) {
        this(dest, source, 1, EntityRef.NULL);
    }

    public ItemMoveEvent(EntityRef source, EntityRef dest, int count) {
        this(dest, source, count, EntityRef.NULL);
    }

    /**
     * @param dest   The destination inventory
     * @param source The source inventory
     * @param item   The item to move
     * @param count  The number of items to move
     */
    public ItemMoveEvent(EntityRef dest, EntityRef source, int count, EntityRef item) {
        destInv = dest;
        sourceInv = source;
        this.item = item;
        this.count = count;
    }

    /**
     * @param count The new number of items to move
     */
    public void setCount(int count) {
        this.count = count;
    }

    /**
     * @return How many items to move
     */
    public int getCount() {
        return count;
    }

    /**
     * @param item The item to move
     */
    public void setItem(EntityRef item) {
        this.item = item == null ? EntityRef.NULL : item;
    }

    /**
     * @return The item to move
     */
    public EntityRef getItem() {
        return item;
    }

    /**
     * @return The source inventory
     */
    public EntityRef getSourceInv() {
        return sourceInv;
    }

    /**
     * @return The destination inventory
     */
    public EntityRef getDestInv() {
        return destInv;
    }

    /**
     * @param value A boolean indicating the success of moving the item.
     */
    public void setSuccess(boolean value) {
        success = value;
    }

    /**
     * @return True if the event was successful in moving the item.
     */
    public boolean wasSuccessful() {
        return success;
    }


    public void setMoveByComponent(Class itemComponent) {
        this.component = itemComponent;
        moveType = 2;
    }

    public void setMoveBySelected() {
        moveType = 1;
    }

    public void setMoveFirstAvailable() {
        moveType = 0;
    }

    public int getMoveType() {
        return moveType;
    }

    public Class getComponent() {
        return component;
    }
}

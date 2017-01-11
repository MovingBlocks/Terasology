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
package org.terasology.logic.inventory.events;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.Event;
import org.terasology.network.ServerEvent;

@ServerEvent
public class MoveItemEvent implements Event {
    private EntityRef sourceInv;
    private EntityRef destInv;
    private int sourceSlot = -1;
    private boolean isHandled;
    private Class component;

    /**
     * Amount of items to move, use -1 for all.
     */
    private int moveAmount = 1;

    /**
     * Indicates the type of move event this is.
     * <p>
     * 0 = Move first entity to first valid slot
     * 1 = Move selected item to first valid slot
     * 2 = Move first entity with component to first valid slot
     */
    private int eventType = 0;

    public MoveItemEvent() {

    }

    public MoveItemEvent(EntityRef sourceInv, EntityRef destInv) {
        this.sourceInv = sourceInv;
        this.destInv = destInv;
    }

    public EntityRef getSourceInv() {
        return sourceInv;
    }

    public EntityRef getDestInv() {
        return destInv;
    }

    public void setMoveByComponent(int amount, Class componentClass) {
        component = componentClass;
        moveAmount = amount;
        eventType = 2;
    }

    public void setMoveSelectedItem(int amount) {
        moveAmount = amount;
        eventType = 1;
    }

    public void setMoveFirstOption(int amount) {
        moveAmount = amount;
        eventType = 0;
    }

    public int getEventType() {
        return eventType;
    }

    public int getCount() {
        return moveAmount;
    }
}

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

/**
 * Removes the entity from the target entity
 */
@ServerEvent
public class RemoveItemEvent implements Event {
    private EntityRef targetEntity = EntityRef.NULL;
    private boolean handled;
    private boolean destroyRemoved = false;
    private int count = 1;

    public RemoveItemEvent() {
    }

    public RemoveItemEvent(EntityRef targetEntity) {
        // ensure that null values do not happen, replace with correct null reference
        this.targetEntity = targetEntity == null ? EntityRef.NULL : targetEntity;
    }

    public EntityRef getTargetEntity() {
        return targetEntity;
    }

    public boolean isHandled() {
        return handled;
    }

    public void setHandled(boolean handled) {
        this.handled = handled;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getCount() {
        return count;
    }

    public void setDestroyRemoved(boolean destroyRemoved) {
        this.destroyRemoved = destroyRemoved;
    }

    public boolean getDestroyRemoved() {
        return destroyRemoved;
    }

}

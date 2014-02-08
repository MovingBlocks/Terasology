/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.logic.inventory.action;

import org.terasology.engine.API;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.AbstractConsumableEvent;

/**
 * Removed the specified item from the inventory of the entity it was sent to. If the remove was successful, the
 * event will be consumed.
 *
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@API
public class RemoveItemAction extends AbstractConsumableEvent {
    private EntityRef instigator;
    private EntityRef item;
    private boolean destroyRemoved;
    private Integer count;

    private EntityRef removedItem;

    public RemoveItemAction(EntityRef instigator, EntityRef item, boolean destroyRemoved) {
        this.instigator = instigator;
        this.item = item;
        this.destroyRemoved = destroyRemoved;
    }

    public RemoveItemAction(EntityRef instigator, EntityRef item, boolean destroyRemoved, int count) {
        this.instigator = instigator;
        this.item = item;
        this.destroyRemoved = destroyRemoved;
        this.count = count;
    }

    public EntityRef getItem() {
        return item;
    }

    public Integer getCount() {
        return count;
    }

    public boolean isDestroyRemoved() {
        return destroyRemoved;
    }

    public void setRemovedItem(EntityRef removedItem) {
        this.removedItem = removedItem;
    }

    public EntityRef getRemovedItem() {
        return removedItem;
    }

    public EntityRef getInstigator() {
        return instigator;
    }
}

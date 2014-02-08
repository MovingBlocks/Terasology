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
 * This action adds the item into the inventory of the entity it was sent to. The item is either completely consumed
 * or not modified at all. If it was consumed, the isConsumed() will return <code>true</code>.
 *
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@API
public class GiveItemAction extends AbstractConsumableEvent {
    private EntityRef instigator;
    private EntityRef item;
    private Integer slot;

    public GiveItemAction(EntityRef instigator, EntityRef item) {
        this.instigator = instigator;
        this.item = item;
    }

    public GiveItemAction(EntityRef instigator, EntityRef item, int slot) {
        this.instigator = instigator;
        this.item = item;
        this.slot = slot;
    }

    public EntityRef getItem() {
        return item;
    }

    public Integer getSlot() {
        return slot;
    }

    public EntityRef getInstigator() {
        return instigator;
    }
}

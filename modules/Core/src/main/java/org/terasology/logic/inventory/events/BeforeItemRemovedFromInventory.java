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
package org.terasology.logic.inventory.events;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.AbstractConsumableEvent;

/**
 */
public class BeforeItemRemovedFromInventory extends AbstractConsumableEvent {
    private EntityRef instigator;
    private EntityRef item;
    private int slot;

    public BeforeItemRemovedFromInventory(EntityRef instigator, EntityRef item, int slot) {
        this.instigator = instigator;
        this.item = item;
        this.slot = slot;
    }

    public EntityRef getItem() {
        return item;
    }

    public int getSlot() {
        return slot;
    }

    public EntityRef getInstigator() {
        return instigator;
    }
}

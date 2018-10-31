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

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.AbstractConsumableEvent;

/**
 * @deprecated Use InventoryManager method instead.
 */
@Deprecated
public class SwitchItemAction extends AbstractConsumableEvent {
    private EntityRef instigator;
    private EntityRef to;
    private int slotFrom;
    private int slotTo;

    public SwitchItemAction(EntityRef instigator, int slotFrom, EntityRef to, int slotTo) {
        this.instigator = instigator;
        this.to = to;
        this.slotFrom = slotFrom;
        this.slotTo = slotTo;
    }

    public EntityRef getTo() {
        return to;
    }

    public int getSlotFrom() {
        return slotFrom;
    }

    public int getSlotTo() {
        return slotTo;
    }

    public EntityRef getInstigator() {
        return instigator;
    }
}

/*
 * Copyright 2013 Moving Blocks
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
package org.terasology.logic.players;

import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.lifecycleEvents.BeforeDeactivateComponent;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.In;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.logic.inventory.SlotBasedInventoryManager;
import org.terasology.logic.inventory.events.InventorySlotChangedEvent;
import org.terasology.logic.players.event.SelectedItemChangedEvent;
import org.terasology.rendering.logic.LightComponent;
import org.terasology.world.block.items.BlockItemComponent;

/**
 * @author Immortius
 */
@RegisterSystem
public class HoldLuminousBlockSystem implements ComponentSystem {
    @In
    private SlotBasedInventoryManager inventoryManager;

    @Override
    public void initialise() {
    }

    @Override
    public void shutdown() {
    }

    @ReceiveEvent
    public void onInventorySlotChanged(InventorySlotChangedEvent event, EntityRef entity, CharacterComponent character) {
        if (character.selectedItem == event.getSlot()) {
            updateLightFromItem(entity, event.getOldItem(), event.getNewItem());
        }
    }

    @ReceiveEvent(components = CharacterComponent.class)
    public void onSelectedItemChanged(SelectedItemChangedEvent event, EntityRef entity) {
        updateLightFromItem(entity, event.getOldItem(), event.getNewItem());
    }

    @ReceiveEvent
    public void onBlockItemDestroyed(BeforeDeactivateComponent event, EntityRef item, BlockItemComponent blockItemComponent) {
        if (blockItemComponent.blockFamily.getArchetypeBlock().getLuminance() == 0) {
            return;
        }

        int slot = inventoryManager.findSlotWithItem(item.getOwner(), item);
        if (slot != -1 && item.getOwner().hasComponent(CharacterComponent.class)) {
            CharacterComponent character = item.getOwner().getComponent(CharacterComponent.class);
            if (slot == character.selectedItem) {
                item.getOwner().removeComponent(LightComponent.class);
            }
        }
    }

    private void updateLightFromItem(EntityRef entity, EntityRef oldItem, EntityRef newItem) {
        byte oldLuminance = getLuminance(oldItem);
        byte newLuminance = getLuminance(newItem);
        if (oldLuminance != newLuminance) {
            if (newLuminance == 0) {
                entity.removeComponent(LightComponent.class);
            } else if (oldLuminance > 0) {
                LightComponent light = entity.getComponent(LightComponent.class);
                light.lightColorAmbient.set(1.0f, 0.6f, 0.6f);
                light.lightColorDiffuse.set(1.0f, 0.6f, 0.6f);
                light.lightDiffuseIntensity = 1.0f;
                light.lightAmbientIntensity = 1.0f;
                entity.saveComponent(light);
            } else {
                LightComponent light = new LightComponent();
                light.lightColorAmbient.set(1.0f, 0.6f, 0.6f);
                light.lightColorDiffuse.set(1.0f, 0.6f, 0.6f);
                light.lightDiffuseIntensity = 1.0f;
                light.lightAmbientIntensity = 1.0f;
                entity.addComponent(light);
            }
        }
    }

    private byte getLuminance(EntityRef item) {
        BlockItemComponent blockItem = item.getComponent(BlockItemComponent.class);
        if (blockItem != null) {
            return blockItem.blockFamily.getArchetypeBlock().getLuminance();
        }
        return 0;
    }


}

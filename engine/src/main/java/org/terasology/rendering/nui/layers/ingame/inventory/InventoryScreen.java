/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.rendering.nui.layers.ingame.inventory;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.logic.inventory.SlotBasedInventoryManager;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.registry.In;
import org.terasology.rendering.nui.UIScreenLayer;
import org.terasology.rendering.nui.databinding.ReadOnlyBinding;

/**
 * @author Immortius
 */
public class InventoryScreen extends UIScreenLayer {

    @In
    private LocalPlayer localPlayer;

    @In
    private SlotBasedInventoryManager inventoryManager;

    @Override
    public void initialise() {
        InventoryGrid inventory = find("inventory", InventoryGrid.class);
        inventory.setTargetEntity(localPlayer.getCharacterEntity());

        TransferItemCursor cursor = find("cursor", TransferItemCursor.class);
        cursor.bindItem(new ReadOnlyBinding<EntityRef>() {
            @Override
            public EntityRef get() {
                CharacterComponent charComp = localPlayer.getCharacterEntity().getComponent(CharacterComponent.class);
                if (charComp != null) {
                    return inventoryManager.getItemInSlot(charComp.movingItem, 0);
                }
                return null;
            }
        });
    }
}

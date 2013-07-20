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

package org.terasology.rendering.gui.widgets;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.terasology.engine.CoreRegistry;
import org.terasology.entitySystem.EntityRef;
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.logic.inventory.SlotBasedInventoryManager;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.rendering.gui.framework.UIDisplayContainer;

import javax.vecmath.Vector2f;

/**
 * @author Immortius
 */
public class UITransferSlotCursor extends UIDisplayContainer {
    private UIItemIcon item;
    private LocalPlayer localPlayer;
    private SlotBasedInventoryManager inventoryManager;

    public UITransferSlotCursor() {
        this.localPlayer = CoreRegistry.get(LocalPlayer.class);
        this.inventoryManager = CoreRegistry.get(SlotBasedInventoryManager.class);
        item = new UIItemIcon(inventoryManager);
        addDisplayElement(item);
    }

    @Override
    public void update() {
        super.update();
        item.setItem(getTransferItem());
        item.setPosition(new Vector2f(Mouse.getX() - getSize().x / 2, Display.getHeight() - Mouse.getY() - getSize().y / 2));
    }

    private EntityRef getTransferEntity() {
        return localPlayer.getCharacterEntity().getComponent(CharacterComponent.class).movingItem;
    }

    private EntityRef getTransferItem() {
        return inventoryManager.getItemInSlot(getTransferEntity(), 0);
    }

}

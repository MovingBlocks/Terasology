/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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
package org.terasology.rendering.gui.menus;

import org.lwjgl.opengl.Display;
import org.terasology.components.InventoryComponent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.events.input.binds.FrobButton;
import org.terasology.events.inventory.ReceiveItemEvent;
import org.terasology.asset.AssetManager;
import org.terasology.rendering.gui.components.UIInventoryNew;
import org.terasology.rendering.gui.framework.UIDisplayWindow;
import org.terasology.rendering.gui.framework.UIGraphicsElement;

import javax.vecmath.Vector2f;

/**
 * Displays two inventories, and allows moving items between them
 *
 * @author Immortius <immortius@gmail.com>
 */
public class UIContainerScreen extends UIDisplayWindow implements UIInventoryNew.InventorySubscriber {
    private static final int CENTER_BORDER = 100;
    private static final int OUTER_BORDER = 50;

    EntityRef container = EntityRef.NULL;
    EntityRef creature = EntityRef.NULL;

    private final UIInventoryNew playerInventory;
    private final UIInventoryNew containerInventory;
    private final UIGraphicsElement background;

    public UIContainerScreen() {
        background = new UIGraphicsElement(AssetManager.loadTexture("engine:containerWindow"));
        background.getTextureSize().set(new Vector2f(256f / 256f, 231f / 256f));
        background.getTextureOrigin().set(new Vector2f(0.0f, 0.0f));
        addDisplayElement(background);

        playerInventory = new UIInventoryNew(4);
        playerInventory.setVisible(true);
        playerInventory.subscribe(this);
        addDisplayElement(playerInventory);

        containerInventory = new UIInventoryNew(4);
        containerInventory.setVisible(true);
        containerInventory.subscribe(this);
        addDisplayElement(containerInventory);

        background.setVisible(true);
        setModal(true);

        layout();
    }

    public void openContainer(EntityRef container, EntityRef creature) {
        this.container = container;
        this.creature = creature;
        playerInventory.setEntity(creature);
        containerInventory.setEntity(container);
    }

    @Override
    public void layout() {
        super.layout();
        playerInventory.setPosition(new Vector2f(0.5f * Display.getWidth() - CENTER_BORDER - playerInventory.getSize().x, 0));
        playerInventory.centerVertically();
        containerInventory.setPosition(new Vector2f(0.5f * Display.getWidth() + CENTER_BORDER, 0));
        containerInventory.centerVertically();
        background.setSize(new Vector2f(2 * (CENTER_BORDER + OUTER_BORDER) + playerInventory.getSize().x + containerInventory.getSize().x, 0.8f * Display.getHeight()));
        background.center();
    }

    public boolean processBindButton(String id, boolean pressed) {
        if (!isVisible() || !pressed)
            return false;

        if (FrobButton.ID == id) {
            setVisible(false);
            return true;
        }
        return false;
    }

    public void itemClicked(UIInventoryNew inventoryNew, int slot) {
        EntityRef fromEntity = inventoryNew.getEntity();
        EntityRef toEntity = (fromEntity.equals(container)) ? creature : container;
        InventoryComponent fromInventory = fromEntity.getComponent(InventoryComponent.class);
        if (fromInventory == null)
            return;
        EntityRef itemEntity = fromInventory.itemSlots.get(slot);
        toEntity.send(new ReceiveItemEvent(itemEntity));
    }

}
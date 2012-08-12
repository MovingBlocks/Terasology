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
package org.terasology.rendering.gui.windows;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.terasology.entitySystem.EntityRef;
import org.terasology.events.input.binds.FrobButton;
import org.terasology.asset.AssetManager;
import org.terasology.rendering.gui.components.UIItemContainer;
import org.terasology.rendering.gui.framework.UIDisplayWindow;
import org.terasology.rendering.gui.framework.UIGraphicsElement;

import javax.vecmath.Vector2f;

/**
 * Displays two inventories, and allows moving items between them
 *
 * @author Immortius <immortius@gmail.com>
 */
public class UIScreenContainer extends UIDisplayWindow {
    private static final int CENTER_BORDER = 100;
    private static final int OUTER_BORDER = 50;

    EntityRef container = EntityRef.NULL;
    EntityRef creature = EntityRef.NULL;

    private final UIItemContainer playerInventory;
    private final UIItemContainer containerInventory;
    private final UIGraphicsElement background;

    public UIScreenContainer() {
        setModal(true);
        setCloseBinds(new String[] {FrobButton.ID});
        setCloseKeys(new int[] {Keyboard.KEY_ESCAPE});
        
        background = new UIGraphicsElement(AssetManager.loadTexture("engine:containerWindow"));
        background.getTextureSize().set(new Vector2f(256f / 256f, 231f / 256f));
        background.getTextureOrigin().set(new Vector2f(0.0f, 0.0f));
        background.setVisible(true);
        
        playerInventory = new UIItemContainer(4);
        playerInventory.setVisible(true);

        containerInventory = new UIItemContainer(4);
        containerInventory.setVisible(true);
        
        addDisplayElement(background);
        addDisplayElement(playerInventory);
        addDisplayElement(containerInventory);

        layout();
    }

    public void openContainer(EntityRef container, EntityRef creature) {
        this.container = container;
        this.creature = creature;
        playerInventory.setEntity(creature);
        containerInventory.setEntity(container);
        
        playerInventory.setConnected(container);
        containerInventory.setConnected(creature);
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
}
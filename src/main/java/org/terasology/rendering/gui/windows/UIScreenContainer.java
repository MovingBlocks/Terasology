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
import org.newdawn.slick.Color;
import org.terasology.entitySystem.EntityRef;
import org.terasology.input.binds.FrobButton;
import org.terasology.asset.AssetManager;
import org.terasology.rendering.gui.widgets.UIImage;
import org.terasology.rendering.gui.widgets.UIItemContainer;
import org.terasology.rendering.gui.widgets.UIWindow;

import javax.vecmath.Vector2f;

/**
 * Displays two inventories, and allows moving items between them
 *
 * @author Immortius <immortius@gmail.com>
 */
public class UIScreenContainer extends UIWindow {
    private static final int CENTER_BORDER = 100;
    private static final int OUTER_BORDER = 50;

    EntityRef container = EntityRef.NULL;
    EntityRef creature = EntityRef.NULL;

    private final UIItemContainer playerInventory;
    private final UIItemContainer containerInventory;
    private final UIImage background;

    public UIScreenContainer() {
        setId("container");
        setBackgroundColor(new Color(0, 0, 0, 200));
        setModal(true);
        maximize();
        setCloseBinds(new String[] {FrobButton.ID});
        setCloseKeys(new int[] {Keyboard.KEY_ESCAPE});
        
        background = new UIImage(AssetManager.loadTexture("engine:containerWindow"));
        background.setTextureSize(new Vector2f(256f, 231f));
        background.setTextureOrigin(new Vector2f(0.0f, 0.0f));
        background.setHorizontalAlign(EHorizontalAlign.CENTER);
        background.setVerticalAlign(EVerticalAlign.CENTER);
        background.setVisible(true);
        
        playerInventory = new UIItemContainer(4);
        playerInventory.setHorizontalAlign(EHorizontalAlign.CENTER);
        playerInventory.setVerticalAlign(EVerticalAlign.CENTER);
        playerInventory.setVisible(true);

        containerInventory = new UIItemContainer(4);
        containerInventory.setHorizontalAlign(EHorizontalAlign.CENTER);
        containerInventory.setVerticalAlign(EVerticalAlign.CENTER);
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
        
        background.setSize(new Vector2f(2 * (CENTER_BORDER + OUTER_BORDER) + playerInventory.getSize().x + containerInventory.getSize().x, 0.8f * Display.getHeight()));
        containerInventory.setPosition(new Vector2f(CENTER_BORDER + containerInventory.getSize().x / 2, 0f));
        playerInventory.setPosition(new Vector2f(-(CENTER_BORDER + playerInventory.getSize().x / 2), 0f));
    }
}

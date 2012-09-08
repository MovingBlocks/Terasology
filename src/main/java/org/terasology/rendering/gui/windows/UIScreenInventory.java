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

import javax.vecmath.Vector2f;

import org.lwjgl.input.Keyboard;
import org.terasology.asset.AssetManager;
import org.terasology.input.binds.InventoryButton;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.LocalPlayer;
import org.terasology.rendering.gui.components.UIItemContainer;
import org.terasology.rendering.gui.framework.UIDisplayWindow;
import org.terasology.rendering.gui.framework.UIGraphicsElement;

/**
 * The player's inventory.
 *
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 */
public class UIScreenInventory extends UIDisplayWindow {

    private final UIItemContainer toolbar;
    private final UIItemContainer inventory;
    private UIGraphicsElement background;

    public UIScreenInventory() {
    	setModal(true);
    	setCloseBinds(new String[] {InventoryButton.ID});
    	setCloseKeys(new int[] {Keyboard.KEY_ESCAPE});
        setSize(new Vector2f(192.0f * 2.5f, 180.0f * 2.5f));
        
        toolbar = new UIItemContainer(9);
        toolbar.setVisible(true);
        toolbar.setCellMargin(new Vector2f(1, 1));
        
        inventory = new UIItemContainer(9);
        inventory.setVisible(true);
        inventory.setCellMargin(new Vector2f(1, 1));

        background = new UIGraphicsElement(AssetManager.loadTexture("engine:inventory"));
        background.setSize(getSize());
        background.getTextureSize().set(new Vector2f(176.0f / 256.0f, 167.0f / 256.0f));
        background.setVisible(true);
        
        addDisplayElement(background);
        addDisplayElement(toolbar);
        addDisplayElement(inventory);

        layout();
    }
    
    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        
        if (visible) {
            toolbar.setEntity(CoreRegistry.get(LocalPlayer.class).getEntity(), 0, 8);
            inventory.setEntity(CoreRegistry.get(LocalPlayer.class).getEntity(), 9);
            //TODO connect toolbar <-> inventory somehow to allow fast transfer.
            
            layout();
        }
    }

    @Override
    public void layout() {
        super.layout();
        
        if (inventory != null) {
            background.center();
            toolbar.center();
            toolbar.getPosition().y += 254;
            inventory.center();
            inventory.getPosition().y += 98;
        }
    }
}

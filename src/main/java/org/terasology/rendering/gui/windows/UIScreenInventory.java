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

import org.terasology.asset.AssetManager;
import org.terasology.events.input.binds.InventoryButton;
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

    private final UIItemContainer inventory;
	private UIGraphicsElement background;

    public UIScreenInventory() {
        setSize(new Vector2f(192.0f * 2.5f, 180.0f * 2.5f));
        
        inventory = new UIItemContainer(9, 4);
        inventory.setVisible(true);
        inventory.setCellMargin(new Vector2f(1, 1));

        background = new UIGraphicsElement(AssetManager.loadTexture("engine:inventory"));
        background.setSize(getSize());
        background.getTextureSize().set(new Vector2f(176.0f / 256.0f, 167.0f / 256.0f));
        background.setVisible(true);
        
        addDisplayElement(background);
        addDisplayElement(inventory);

        layout();
        setModal(true);
    }
    
    @Override
    public void setVisible(boolean visible) {
    	super.setVisible(visible);
    	
    	if (visible)
            inventory.setEntity(CoreRegistry.get(LocalPlayer.class).getEntity());
    }

    @Override
    public void layout() {
        super.layout();
        
        if (inventory != null) {
        	background.center();
        	inventory.center();
        	inventory.getPosition().y += 48;
        	
        	if (inventory.getCells().size() >= 9) {
        		float toolbarPos = calcAbsolutePosition().y + 208;
	        	for (int i = 0; i < 9; i++) {
	        		inventory.getCells().get(i).getPosition().y = toolbarPos;		
				}
        	}
        }
    }
    
    @Override
    public boolean processBindButton(String id, boolean pressed) {
        if (pressed && InventoryButton.ID.equals(id)) {
            close(true);
            return true;
        }
        
        return false;
    }
}

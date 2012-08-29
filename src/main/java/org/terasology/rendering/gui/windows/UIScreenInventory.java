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
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.events.MouseButtonListener;
import org.terasology.rendering.gui.framework.events.WindowListener;
import org.terasology.rendering.gui.widgets.UIImage;
import org.terasology.rendering.gui.widgets.UIItemCell;
import org.terasology.rendering.gui.widgets.UIItemContainer;
import org.terasology.rendering.gui.widgets.UIWindow;

/**
 * The player's inventory.
 *
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 */
public class UIScreenInventory extends UIWindow {

    private final UIItemContainer toolbar;
    private final UIItemContainer inventory;
    private UIImage background;

    public UIScreenInventory() {
        setBackgroundColor(0x00, 0x00, 0x00, 0.75f);
        setModal(true);
        setCloseBinds(new String[] {InventoryButton.ID});
        setCloseKeys(new int[] {Keyboard.KEY_ESCAPE});
        maximize();
        
        addWindowListener(new WindowListener() {
            @Override
            public void open(UIDisplayElement element) {
                toolbar.setEntity(CoreRegistry.get(LocalPlayer.class).getEntity(), 0, 8);
                inventory.setEntity(CoreRegistry.get(LocalPlayer.class).getEntity(), 9);
                //TODO connect toolbar <-> inventory somehow to allow fast transfer.
                
                layout();
            }
            
            @Override
            public void close(UIDisplayElement element) {
                
            }
        });
        
        addMouseButtonListener(new MouseButtonListener() {
            
            @Override
            public void wheel(UIDisplayElement element, int wheel, boolean intersect) {
                
            }
            
            @Override
            public void up(UIDisplayElement element, int button, boolean intersect) {
                
            }
            
            @Override
            public void down(UIDisplayElement element, int button, boolean intersect) {
                if (button == 0) {
                    UIItemCell.reset(); //TODO drop item
                }
            }
        });
        
        toolbar = new UIItemContainer(9);
        toolbar.setCellMargin(new Vector2f(1, 1));
        toolbar.setHorizontalAlign(EHorizontalAlign.CENTER);
        toolbar.setVerticalAlign(EVerticalAlign.CENTER);
        toolbar.setPosition(new Vector2f(0f, 180f));
        toolbar.setVisible(true);
        
        inventory = new UIItemContainer(9);
        inventory.setCellMargin(new Vector2f(1, 1));
        inventory.setHorizontalAlign(EHorizontalAlign.CENTER);
        inventory.setVerticalAlign(EVerticalAlign.CENTER);
        inventory.setPosition(new Vector2f(0f, 72f));
        inventory.setVisible(true);

        background = new UIImage(AssetManager.loadTexture("engine:inventory"));
        background.setSize(new Vector2f(192.0f * 2.5f, 180.0f * 2.5f));
        background.setTextureSize(new Vector2f(176.0f, 167.0f));
        background.setHorizontalAlign(EHorizontalAlign.CENTER);
        background.setVerticalAlign(EVerticalAlign.CENTER);
        background.setVisible(true);
        
        addDisplayElement(background);
        addDisplayElement(toolbar);
        addDisplayElement(inventory);
    }
}

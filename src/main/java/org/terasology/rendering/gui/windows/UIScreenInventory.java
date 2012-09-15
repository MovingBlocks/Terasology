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
import javax.vecmath.Vector4f;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.newdawn.slick.Color;
import org.terasology.asset.AssetManager;
import org.terasology.input.binds.InventoryButton;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.LocalPlayer;
import org.terasology.logic.manager.GUIManager;
import org.terasology.rendering.gui.animation.AnimationMove;
import org.terasology.rendering.gui.animation.AnimationRotate;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.events.AnimationListener;
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

    private final UIImage leftGearWheel;
    private final UIImage rightGearWheel;

    //Todo this is a temporary solution
    private boolean setVisible = true;

    public UIScreenInventory() {
        setId("inventory");
        setBackgroundColor(new Color(0, 0, 0, 200));
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

                GUIManager.getInstance().getWindowById("hud").getElementById("leftGearWheel").setVisible(false);
                GUIManager.getInstance().getWindowById("hud").getElementById("rightGearWheel").setVisible(false);

                inventory.setPosition(new Vector2f(toolbar.getAbsolutePosition().x, Display.getHeight() + 5f));
                inventory.setAnimation(new AnimationMove(new Vector2f(Display.getWidth() / 2 - inventory.getSize().x / 2, Display.getHeight() - 192f), 20f));
                inventory.getAnimation(AnimationMove.class).start();
                leftGearWheel.setAnimation(new AnimationRotate(-120f,10f));
                leftGearWheel.getAnimation(AnimationRotate.class).start();
                rightGearWheel.setAnimation(new AnimationRotate(120f,10f));
                rightGearWheel.getAnimation(AnimationRotate.class).start();
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
        toolbar.setVisible(true);
        toolbar.setHorizontalAlign(EHorizontalAlign.CENTER);
        toolbar.setVerticalAlign(EVerticalAlign.BOTTOM);
        toolbar.setCellMargin(new Vector2f(0f, 0f));
        toolbar.setBorderImage("engine:inventory", new Vector2f(0f, 84f), new Vector2f(169f, 83f), new Vector4f(4f, 4f, 4f, 4f));

        inventory = new UIItemContainer(9);
        inventory.setPosition(new Vector2f(0f, Display.getHeight() + 5f));
        inventory.setVisible(true);
        inventory.setCellMargin(new Vector2f(0f, 0f));
        inventory.setBorderImage("engine:inventory", new Vector2f(0f, 84f), new Vector2f(169f, 61f), new Vector4f(5f, 4f, 3f, 4f));

        leftGearWheel = new UIImage(AssetManager.loadTexture("engine:inventory"));
        leftGearWheel.setSize(new Vector2f(36f, 36f));
        leftGearWheel.setTextureOrigin(new Vector2f(121.0f, 168.0f));
        leftGearWheel.setTextureSize(new Vector2f(27.0f, 27.0f));
        leftGearWheel.setVisible(true);

        rightGearWheel = new UIImage(AssetManager.loadTexture("engine:inventory"));
        rightGearWheel.setSize(new Vector2f(36f, 36f));
        rightGearWheel.setTextureOrigin(new Vector2f(121.0f, 168.0f));
        rightGearWheel.setTextureSize(new Vector2f(27.0f, 27.0f));
        rightGearWheel.setVisible(true);

        addDisplayElement(rightGearWheel);
        addDisplayElement(leftGearWheel);
        addDisplayElement(inventory);
        addDisplayElement(toolbar);
    }

    @Override
    public void update(){
        if(leftGearWheel != null && rightGearWheel != null){
            leftGearWheel.setPosition(new Vector2f(
                    toolbar.getPosition().x - leftGearWheel.getSize().x/2,
                    toolbar.getPosition().y)
            );
            rightGearWheel.setPosition(new Vector2f(
                    toolbar.getPosition().x + toolbar.getSize().x - rightGearWheel.getSize().x/2,
                    toolbar.getPosition().y)
            );
        }
        inventory.setPosition(new Vector2f(Display.getWidth()/2 - inventory.getSize().x/2, inventory.getPosition().y));
        super.update();
    }

    @Override
    public void setVisible(boolean visible){
        if(!setVisible){
            super.setVisible(false);
            setVisible = true;
            return;
        }
        if(!visible){
            inventory.setAnimation(new AnimationMove(new Vector2f(inventory.getPosition().x, Display.getHeight() + 5f), 20f));
            inventory.addAnimationListener(AnimationMove.class, new AnimationListener() {
                
                @Override
                public void stop(UIDisplayElement element) {
                    
                }
                
                @Override
                public void start(UIDisplayElement element) {
                    GUIManager.getInstance().getWindowById("hud").getElementById("leftGearWheel").setVisible(true);
                    GUIManager.getInstance().getWindowById("hud").getElementById("rightGearWheel").setVisible(true);
                    element.getParent().getParent().setVisible(false);
                }
                
                @Override
                public void repeat(UIDisplayElement element) {
                    
                }
            });
            setVisible = false;
            inventory.getAnimation(AnimationMove.class).start();
            leftGearWheel.setAnimation(new AnimationRotate(360f,10f));
            leftGearWheel.getAnimation(AnimationRotate.class).start();
            rightGearWheel.setAnimation(new AnimationRotate(-360f,10f));
            rightGearWheel.getAnimation(AnimationRotate.class).start();
        }else{
            super.setVisible(true);
        }
    }
}

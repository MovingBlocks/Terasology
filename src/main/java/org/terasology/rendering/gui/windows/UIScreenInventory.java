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
import org.terasology.game.types.GameType;
import org.terasology.input.binds.InventoryButton;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.LocalPlayer;
import org.terasology.logic.manager.GUIManager;
import org.terasology.rendering.gui.animation.AnimationMove;
import org.terasology.rendering.gui.animation.AnimationRotate;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.events.AnimationListener;
import org.terasology.rendering.gui.framework.events.MouseButtonListener;
import org.terasology.rendering.gui.framework.events.VisibilityListener;
import org.terasology.rendering.gui.layout.GridLayout;
import org.terasology.rendering.gui.widgets.*;

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
        setCloseBinds(new String[] {"engine:inventory"});
        setCloseKeys(new int[] {Keyboard.KEY_ESCAPE});
        maximize();
        
        addVisibilityListener(new VisibilityListener() {
            @Override
            public void changed(UIDisplayElement element, boolean visibility) {
                if (visibility) {
                    toolbar.setEntity(CoreRegistry.get(LocalPlayer.class).getEntity(), 0, 9);
                    inventory.setEntity(CoreRegistry.get(LocalPlayer.class).getEntity(), 10);
                    //TODO connect toolbar <-> inventory somehow to allow fast transfer.

                    getGUIManager().getWindowById("hud").getElementById("leftGearWheel").setVisible(false);
                    getGUIManager().getWindowById("hud").getElementById("rightGearWheel").setVisible(false);
                    layout();
                    inventory.setPosition(new Vector2f(Display.getWidth()/2 - inventory.getSize().x/2, Display.getHeight() + 5f));
                    inventory.addAnimation(new AnimationMove(new Vector2f(Display.getWidth() / 2 - inventory.getSize().x / 2, Display.getHeight() - 192f), 20f));
                    inventory.getAnimation(AnimationMove.class).start();

                    leftGearWheel.addAnimation(new AnimationRotate(-120f,10f));
                    leftGearWheel.getAnimation(AnimationRotate.class).start();
                    rightGearWheel.addAnimation(new AnimationRotate(120f,10f));
                    rightGearWheel.getAnimation(AnimationRotate.class).start();
                }else{
                    getGUIManager().getWindowById("hud").getElementById("leftGearWheel").setVisible(true);
                    getGUIManager().getWindowById("hud").getElementById("rightGearWheel").setVisible(true);
                }
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

        CoreRegistry.get(GameType.class).onCreateInventoryHook(this);

        toolbar = new UIItemContainer(10);
        toolbar.setVisible(true);
        toolbar.setHorizontalAlign(EHorizontalAlign.CENTER);
        toolbar.setVerticalAlign(EVerticalAlign.BOTTOM);
        toolbar.setCellMargin(new Vector2f(0f, 0f));
        toolbar.setBorderImage("engine:inventory", new Vector2f(0f, 84f), new Vector2f(169f, 83f), new Vector4f(4f, 4f, 4f, 4f));

        inventory = new UIItemContainer(10);
        inventory.setVisible(true);
        inventory.setCellMargin(new Vector2f(0f, 0f));
        inventory.setBorderImage("engine:inventory", new Vector2f(0f, 84f), new Vector2f(169f, 61f), new Vector4f(5f, 4f, 3f, 4f));

        leftGearWheel = new UIImage(AssetManager.loadTexture("engine:inventory"));
        leftGearWheel.setSize(new Vector2f(36f, 36f));
        leftGearWheel.setTextureOrigin(new Vector2f(121.0f, 168.0f));
        leftGearWheel.setTextureSize(new Vector2f(27.0f, 27.0f));
        leftGearWheel.setVisible(true);

        leftGearWheel.setHorizontalAlign(EHorizontalAlign.CENTER);
        leftGearWheel.setVerticalAlign(EVerticalAlign.BOTTOM);
        leftGearWheel.setPosition(new Vector2f(
                leftGearWheel.getPosition().x - 240f,
                leftGearWheel.getPosition().y - 4f)
        );

        rightGearWheel = new UIImage(AssetManager.loadTexture("engine:inventory"));
        rightGearWheel.setSize(new Vector2f(36f, 36f));
        rightGearWheel.setTextureOrigin(new Vector2f(121.0f, 168.0f));
        rightGearWheel.setTextureSize(new Vector2f(27.0f, 27.0f));
        rightGearWheel.setVisible(true);

        rightGearWheel.setHorizontalAlign(EHorizontalAlign.CENTER);
        rightGearWheel.setVerticalAlign(EVerticalAlign.BOTTOM);
        rightGearWheel.setPosition(new Vector2f(
                rightGearWheel.getPosition().x + 240f,
                rightGearWheel.getPosition().y - 4f)
        );

        addDisplayElement(rightGearWheel);
        addDisplayElement(leftGearWheel);
        addDisplayElement(inventory);
        addDisplayElement(toolbar);
    }
}

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
import org.terasology.logic.manager.GUIManager;
import org.terasology.rendering.gui.animation.AnimationMove;
import org.terasology.rendering.gui.animation.AnimationRotate;
import org.terasology.rendering.gui.widgets.UIImage;
import org.terasology.rendering.gui.widgets.UIItemContainer;
import org.terasology.rendering.gui.widgets.UIWindow;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector4f;

/**
 * Displays two inventories, and allows moving items between them
 *
 * @author Immortius <immortius@gmail.com>
 */
public class UIScreenContainer extends UIWindow {

    EntityRef container = EntityRef.NULL;
    EntityRef creature = EntityRef.NULL;

    private final UIItemContainer playerInventory;
    private final UIItemContainer playerToolbar;
    private final UIItemContainer containerInventory;

    private final UIImage leftGearWheel;
    private final UIImage rightGearWheel;

    public UIScreenContainer() {
        setId("container");
        setBackgroundColor(new Color(0, 0, 0, 200));
        setModal(true);
        maximize();
        setCloseBinds(new String[] {FrobButton.ID});
        setCloseKeys(new int[] {Keyboard.KEY_ESCAPE});

        playerToolbar = new UIItemContainer(10);
        playerToolbar.setVisible(true);
        playerToolbar.setHorizontalAlign(EHorizontalAlign.CENTER);
        playerToolbar.setVerticalAlign(EVerticalAlign.BOTTOM);
        playerToolbar.setCellMargin(new Vector2f(0f, 0f));
        playerToolbar.setBorderImage("engine:inventory", new Vector2f(0f, 84f), new Vector2f(169f, 83f), new Vector4f(4f, 4f, 4f, 4f));

        playerInventory = new UIItemContainer(10);
        playerInventory.setVisible(true);
        playerInventory.setCellMargin(new Vector2f(0f, 0f));
        playerInventory.setBorderImage("engine:inventory", new Vector2f(0f, 84f), new Vector2f(169f, 61f), new Vector4f(5f, 4f, 3f, 4f));

        containerInventory = new UIItemContainer(10);
        containerInventory.setVisible(true);
        containerInventory.setHorizontalAlign(EHorizontalAlign.CENTER);
        containerInventory.setCellMargin(new Vector2f(0f, 0f));
        containerInventory.setBorderImage("engine:inventory", new Vector2f(0f, 84f), new Vector2f(169f, 61f), new Vector4f(5f, 4f, 3f, 4f));

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

        addDisplayElement(playerInventory);
        addDisplayElement(playerToolbar);
        addDisplayElement(containerInventory);

        layout();
    }


    @Override
    public void layout(){
        super.layout();
        if(leftGearWheel != null && rightGearWheel != null){
            leftGearWheel.setPosition(new Vector2f(
                    playerToolbar.getPosition().x - leftGearWheel.getSize().x/2,
                    playerToolbar.getPosition().y)
            );
            rightGearWheel.setPosition(new Vector2f(
                    playerToolbar.getPosition().x + playerToolbar.getSize().x - rightGearWheel.getSize().x/2,
                    playerToolbar.getPosition().y)
            );
        }
        if(playerInventory != null){
            playerInventory.setPosition(new Vector2f(Display.getWidth()/2 - playerInventory.getSize().x/2, playerInventory.getPosition().y));
        }

        if(containerInventory != null){
            containerInventory.setPosition(new Vector2f(0f, Display.getHeight() - 212f - containerInventory.getSize().y));
        }
    }

    public void openContainer(EntityRef container, EntityRef creature) {
        this.container = container;
        this.creature = creature;

        playerToolbar.setEntity(creature,0, 9);
        playerInventory.setEntity(creature,10);
        containerInventory.setEntity(container);

        playerToolbar.setConnected(container);
        playerInventory.setConnected(container);
        containerInventory.setConnected(creature);
        //TODO connect toolbar <-> inventory somehow to allow fast transfer.

        GUIManager.getInstance().getWindowById("hud").getElementById("leftGearWheel").setVisible(false);
        GUIManager.getInstance().getWindowById("hud").getElementById("rightGearWheel").setVisible(false);
        layout();
        playerInventory.setPosition(new Vector2f(Display.getWidth()/2 - playerInventory.getSize().x/2, Display.getHeight() + 5f));
        playerInventory.setAnimation(new AnimationMove(new Vector2f(Display.getWidth() / 2 - playerInventory.getSize().x / 2, Display.getHeight() - 192f), 20f));
        playerInventory.getAnimation(AnimationMove.class).start();
        leftGearWheel.setAnimation(new AnimationRotate(-120f,10f));
        leftGearWheel.getAnimation(AnimationRotate.class).start();
        rightGearWheel.setAnimation(new AnimationRotate(120f,10f));
        rightGearWheel.getAnimation(AnimationRotate.class).start();
    }
}

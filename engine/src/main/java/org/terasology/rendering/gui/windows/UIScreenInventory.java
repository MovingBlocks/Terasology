/*
 * Copyright 2013 MovingBlocks
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
import org.terasology.asset.Assets;
import org.terasology.registry.CoreRegistry;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.rendering.gui.animation.AnimationMove;
import org.terasology.rendering.gui.animation.AnimationRotate;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.events.MouseButtonListener;
import org.terasology.rendering.gui.widgets.UIImage;
import org.terasology.rendering.gui.widgets.UIInventoryGrid;
import org.terasology.rendering.gui.widgets.UITransferSlotCursor;
import org.terasology.rendering.gui.widgets.UIWindow;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector4f;

/**
 * The player's inventory.
 *
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 */
public class UIScreenInventory extends UIWindow {

    private final UIInventoryGrid toolbar;
    private final UIInventoryGrid inventory;

    private final UIImage leftGearWheel;
    private final UIImage rightGearWheel;

    public UIScreenInventory() {
        setId("inventory");
        setBackgroundColor(new Color(0, 0, 0, 200));
        setModal(true);
        setCloseKeys(new int[]{Keyboard.KEY_ESCAPE});
        setCloseBinds(new String[]{"engine:inventory"});
        maximize();
        addMouseButtonListener(new MouseButtonListener() {

            @Override
            public void wheel(UIDisplayElement element, int wheel, boolean intersect) {

            }

            @Override
            public void up(UIDisplayElement element, int button, boolean intersect) {

            }

            @Override
            public void down(UIDisplayElement element, int button, boolean intersect) {
                // TODO: drop item
            }
        });

        toolbar = new UIInventoryGrid(10);
        toolbar.setVisible(true);
        toolbar.setHorizontalAlign(EHorizontalAlign.CENTER);
        toolbar.setVerticalAlign(EVerticalAlign.BOTTOM);
        toolbar.setCellMargin(new Vector2f(0f, 0f));
        toolbar.setBorderImage("engine:inventory", new Vector2f(0f, 84f), new Vector2f(169f, 83f), new Vector4f(4f, 4f, 4f, 4f));

        inventory = new UIInventoryGrid(10);
        inventory.setVisible(true);
        inventory.setCellMargin(new Vector2f(0f, 0f));
        inventory.setBorderImage("engine:inventory", new Vector2f(0f, 84f), new Vector2f(169f, 61f), new Vector4f(5f, 4f, 3f, 4f));
        inventory.setId("inventory");

        leftGearWheel = new UIImage(Assets.getTexture("engine:inventory"));
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

        rightGearWheel = new UIImage(Assets.getTexture("engine:inventory"));
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
        addDisplayElement(new UITransferSlotCursor());
    }

    @Override
    public void update() {
        super.update();    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void close() {
        super.close();
        getGUIManager().getWindowById("hud").getElementById("leftGearWheel").setVisible(true);
        getGUIManager().getWindowById("hud").getElementById("rightGearWheel").setVisible(true);
    }

    @Override
    public void open() {
        super.open();
        toolbar.linkToEntity(CoreRegistry.get(LocalPlayer.class).getCharacterEntity(), 0, 10);
        inventory.linkToEntity(CoreRegistry.get(LocalPlayer.class).getCharacterEntity(), 10);
        //TODO connect toolbar <-> inventory somehow to allow fast transfer.

        getGUIManager().getWindowById("hud").getElementById("leftGearWheel").setVisible(false);
        getGUIManager().getWindowById("hud").getElementById("rightGearWheel").setVisible(false);
        layout();
        inventory.setPosition(new Vector2f(Display.getWidth() / 2 - inventory.getSize().x / 2, Display.getHeight() + 5f));
        inventory.addAnimation(new AnimationMove(new Vector2f(Display.getWidth() / 2 - inventory.getSize().x / 2, Display.getHeight() - 192f), 20f));
        inventory.getAnimation(AnimationMove.class).start();

        leftGearWheel.addAnimation(new AnimationRotate(-120f, 10f));
        leftGearWheel.getAnimation(AnimationRotate.class).start();
        rightGearWheel.addAnimation(new AnimationRotate(120f, 10f));
        rightGearWheel.getAnimation(AnimationRotate.class).start();
    }


    /**
     * Drop the item in the transfer slot.
     * TODO this needs some work.
     */
//    public void dropitem() {
//
//        if (item.exists()) {
//            ItemComponent itemComp = item.getComponent(ItemComponent.class);
//            BlockItemComponent blockItem = item.getComponent(BlockItemComponent.class);
//
//            if (blockItem != null) {
//                int dropPower = 6;
//                EntityManager entityManager = CoreRegistry.get(EntityManager.class);
//                LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);
//                LocalPlayerComponent localPlayerComp = localPlayer.getCharacterEntity().getComponent(LocalPlayerComponent.class);
//                BlockPickupFactory droppedBlockFactory = new BlockPickupFactory(entityManager);
//                Vector3f pos = new Vector3f(
//                        localPlayer.getPosition().x + localPlayer.getViewDirection().x * 1.5f,
//                        localPlayer.getPosition().y + localPlayer.getViewDirection().y * 1.5f,
//                        localPlayer.getPosition().z + localPlayer.getViewDirection().z * 1.5f);
//                EntityRef droppedBlock = droppedBlockFactory.newInstance(pos, blockItem.blockFamily, 20);
//
//                for (int i = 0; i < itemComp.stackCount; i++) {
//                    Vector3f impulse = new Vector3f(
//                            localPlayer.getViewDirection().x * dropPower,
//                            localPlayer.getViewDirection().y * dropPower,
//                            localPlayer.getViewDirection().z * dropPower);
//                    droppedBlock.send(new ImpulseEvent(impulse));
//                }
//
//                localPlayerComp.handAnimation = 0.5f;
//            }
//        }
//    }
}

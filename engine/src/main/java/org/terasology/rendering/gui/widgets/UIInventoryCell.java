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
package org.terasology.rendering.gui.widgets;

import org.terasology.asset.Assets;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.input.Keyboard;
import org.terasology.input.MouseInput;
import org.terasology.input.events.KeyEvent;
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.logic.common.DisplayNameComponent;
import org.terasology.logic.inventory.InventoryUtils;
import org.terasology.logic.inventory.action.MoveItemAction;
import org.terasology.logic.inventory.action.SwitchItemAction;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.gui.framework.UIDisplayContainer;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.events.KeyListener;
import org.terasology.rendering.gui.framework.events.MouseButtonListener;
import org.terasology.rendering.gui.framework.events.MouseMoveListener;

import javax.vecmath.Vector2f;

/**
 * A cell which is linked to a slot-based inventory.
 */
public class UIInventoryCell extends UIDisplayContainer {
    private static final Vector2f DEFAULT_ICON_POSITION = new Vector2f(2f, 2f);
    private static final Vector2f ITEM_LABEL_POSITION = new Vector2f(0f, -14f);

    //entity
    private EntityRef inventoryEntity = EntityRef.NULL;
    private int slot;

    private LocalPlayer localPlayer;

    //sub elements
    private final UIImage selectionRectangle;
    private final UIImage background;
    private final UILabel itemLabel;
    private UIItemIcon icon;

    private boolean selected;
    private boolean selectOnMouseOver = true;

    private boolean multiplierKeyPressed;

    private MouseMoveListener mouseMoveListener = new MouseMoveListener() {
        @Override
        public void leave(UIDisplayElement element) {
            itemLabel.setVisible(false);

            if (selectOnMouseOver) {
                selectionRectangle.setVisible(selected);
            }
        }

        @Override
        public void hover(UIDisplayElement element) {

        }

        @Override
        public void enter(UIDisplayElement element) {
            itemLabel.setVisible(true);

            if (selectOnMouseOver) {
                selectionRectangle.setVisible(true);
            }
        }

        @Override
        public void move(UIDisplayElement element) {
        }
    };

    private MouseButtonListener mouseButtonListener = new MouseButtonListener() {
        @Override
        public void wheel(UIDisplayElement element, int wheel, boolean intersect) {
            if (intersect) {

                int amount = (multiplierKeyPressed) ? 2 : 1;

                //move item to the transfer slot
                if (wheel > 0) {
                    takeAmount(amount);
                } else {
                    //get item from transfer slot
                    giveAmount(amount);
                }
            }
        }

        @Override
        public void up(UIDisplayElement element, int button, boolean intersect) {

        }

        @Override
        public void down(UIDisplayElement element, int button, boolean intersect) {
            if (intersect) {
                if (MouseInput.MOUSE_LEFT.getId() == button) {
                    swapItem();
                } else if (MouseInput.MOUSE_RIGHT.getId() == button) {
                    EntityRef item = InventoryUtils.getItemAt(inventoryEntity, slot);
                    int stackSize = InventoryUtils.getStackCount(item);
                    if (stackSize > 0) {
                        giveAmount((stackSize + 1) / 2);
                    }
                }
            }
        }
    };

    private KeyListener keyListener = new KeyListener() {
        @Override
        public void key(UIDisplayElement element, KeyEvent event) {
            if (event.getKey() == Keyboard.Key.LEFT_CTRL) {
                multiplierKeyPressed = event.isDown();
            }
        }
    };

    public UIInventoryCell(EntityRef inventoryEntity, int slot, Vector2f size) {
        this(inventoryEntity, slot, size, DEFAULT_ICON_POSITION);
    }

    /**
     * Create a single item cell which is capable of holding an item.
     *
     * @param inventoryEntity The inventoryEntity of this item.
     * @param size            The size of the icon cell.
     * @param iconPosition    The position of the icon cell.
     */
    public UIInventoryCell(EntityRef inventoryEntity, int slot, Vector2f size, Vector2f iconPosition) {
        this.inventoryEntity = inventoryEntity;
        this.slot = slot;
        this.localPlayer = CoreRegistry.get(LocalPlayer.class);

        setSize(size);

        Texture guiTex = Assets.getTexture("engine:gui");

        selectionRectangle = new UIImage(guiTex);
        selectionRectangle.setTextureSize(new Vector2f(22f, 22f));
        selectionRectangle.setTextureOrigin(new Vector2f(1f, 23f));
        selectionRectangle.setSize(new Vector2f(getSize().x, getSize().y));
        selectionRectangle.setVisible(false);

        background = new UIImage(Assets.getTexture("engine:inventory"));
        background.setTextureSize(new Vector2f(19f, 19f));
        background.setTextureOrigin(new Vector2f(3f, 146f));
        background.setSize(getSize());
        background.setVisible(true);
        background.setFixed(true);

        itemLabel = new UILabel();
        itemLabel.setVisible(false);
        itemLabel.setPosition(ITEM_LABEL_POSITION);

        icon = new UIItemIcon();
        icon.setPosition(iconPosition);
        icon.setVisible(true);

        addMouseMoveListener(mouseMoveListener);
        addMouseButtonListener(mouseButtonListener);
        addKeyListener(keyListener);

        addDisplayElement(background);
        addDisplayElement(icon);
        addDisplayElement(selectionRectangle);
        addDisplayElement(itemLabel);
    }

    @Override
    public void update() {
        super.update();
        EntityRef item = InventoryUtils.getItemAt(inventoryEntity, slot);
        icon.setItem(item);
        if (itemLabel.isVisible()) {
            itemLabel.setText(getLabelFor(item));
        }
    }

    private String getLabelFor(EntityRef item) {
        DisplayNameComponent info = item.getComponent(DisplayNameComponent.class);
        if (info != null) {
            return info.name;
        }
        return "";
    }

    private EntityRef getTransferEntity() {
        return localPlayer.getCharacterEntity().getComponent(CharacterComponent.class).movingItem;
    }

    private EntityRef getTransferItem() {
        return InventoryUtils.getItemAt(getTransferEntity(), 0);
    }

    private EntityRef getItem() {
        return InventoryUtils.getItemAt(inventoryEntity, slot);
    }

    private void swapItem() {
        getTransferEntity().send(new SwitchItemAction(0, inventoryEntity, slot));
    }

    private void giveAmount(int amount) {
        inventoryEntity.send(new MoveItemAction(slot, getTransferEntity(), 0, amount));
    }

    private void takeAmount(int amount) {
        getTransferEntity().send(new MoveItemAction(0, inventoryEntity, slot, amount));
    }

    /**
     * Check if the cell shows an item count.
     *
     * @return Returns true if the cell shows an item count.
     */
    public boolean isDisplayingItemCount() {
        return icon.isDisplayingItemCount();
    }

    /**
     * Set if the cell shows an item count.
     *
     * @param enable True to display an item count.
     */
    public void setDisplayingItemCount(boolean enable) {
        icon.setDisplayingItemCount(enable);
    }

    /**
     * Check whether the selection rectangle is shown.
     *
     * @return Returns true if the selection rectangle is shown.
     */
    public boolean isSelected() {
        return selectionRectangle.isVisible();
    }

    /**
     * Set the visibility of the selection rectangle.
     *
     * @param enable True to enable the selection rectangle.
     */
    public void setSelected(boolean enable) {
        selected = enable;
        selectionRectangle.setVisible(enable);
    }
}

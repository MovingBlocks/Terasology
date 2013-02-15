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
package org.terasology.rendering.gui.widgets;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.terasology.asset.Assets;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.game.CoreRegistry;
import org.terasology.input.MouseInput;
import org.terasology.input.events.KeyEvent;
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.logic.inventory.InventoryComponent;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.inventory.InventorySystem;
import org.terasology.logic.inventory.ItemComponent;
import org.terasology.logic.inventory.SlotBasedInventoryManager;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.model.inventory.Icon;
import org.terasology.rendering.assets.Texture;
import org.terasology.rendering.gui.framework.UIDisplayContainer;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.events.KeyListener;
import org.terasology.rendering.gui.framework.events.MouseButtonListener;
import org.terasology.rendering.gui.framework.events.MouseMoveListener;
import org.terasology.world.block.Block;
import org.terasology.world.block.entity.BlockItemComponent;
import org.terasology.world.block.family.BlockFamily;

import javax.vecmath.Vector2f;

import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glTranslatef;

/**
 * A cell which can contain an item and supports drag and drop.
 * To move an item the item will be moved to a special transfer slot as item will be dragged. This slot is in the CharacterComponent class.
 * Therefore the item belongs to nobody as the transfer is ongoing and needs to be reseted if the action was interrupted.
 *
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 * @see org.terasology.logic.characters.CharacterComponent
 */
public class UIItemCell extends UIDisplayContainer {

    //transfer
    private static UIItemCellIcon transferIcon;

    //entity
    private EntityRef ownerEntity = EntityRef.NULL;
    private EntityRef itemEntity = EntityRef.NULL;
    private int slot;

    //connected inventory entity
    private EntityRef connectedEntity = EntityRef.NULL;
    private boolean instantTransferKeyPressed = false;

    //sub elements
    private final UIImage selectionRectangle;
    private final UIImage background;
    private final UILabel itemLabel;
    private UIItemCellIcon icon;

    //layout
    private Vector2f itemLabelPosition = new Vector2f(0f, -14f);
    private static Vector2f iconPosition = new Vector2f(2f, 2f);

    //settings
    private boolean enableDrag = true;
    private boolean enableSelectionRectangle = true;
    private boolean multiplierKeyPressed = false;

    private MouseMoveListener mouseMoveListener = new MouseMoveListener() {
        @Override
        public void leave(UIDisplayElement element) {
            setLabelVisibility(false);

            if (enableSelectionRectangle) {
                selectionRectangle.setVisible(false);
            }
        }

        @Override
        public void hover(UIDisplayElement element) {

        }

        @Override
        public void enter(UIDisplayElement element) {
            setLabelVisibility(true);

            if (enableSelectionRectangle) {
                selectionRectangle.setVisible(true);
            }
        }

        @Override
        public void move(UIDisplayElement element) {
            if (!enableDrag)
                return;

        }
    };

    private MouseButtonListener mouseButtonListener = new MouseButtonListener() {
        @Override
        public void wheel(UIDisplayElement element, int wheel, boolean intersect) {
            if (intersect) {
                if (!enableDrag)
                    return;

                int amount = (multiplierKeyPressed) ? 2 : 1;

                //move item to the transfer slot
                if (wheel > 0) {
                    giveAmount(amount);
                }
                //get item from transfer slot
                else {
                    takeAmount(amount);
                }
            }
        }

        @Override
        public void up(UIDisplayElement element, int button, boolean intersect) {

        }

        @Override
        public void down(UIDisplayElement element, int button, boolean intersect) {
            if (intersect) {
                if (!enableDrag)
                    return;

                if (ownerEntity.getComponent(InventoryComponent.class) != null) {
                    if (MouseInput.MOUSE_LEFT.getId() == button) {
                        if (getTransferItem().exists()) {
                            swapItem();
                        } else {
                            if (instantTransferKeyPressed && connectedEntity.exists()) {
                                moveItemDirect();
                            } else {
                                swapItem();
                            }
                        }
                    } else if (MouseInput.MOUSE_RIGHT.getId() == button) {
                        if (getTransferItem().exists()) {
                            int amount = (multiplierKeyPressed) ? 2 : 1;
                            giveAmount(amount);
                        } else {
                            ItemComponent item = itemEntity.getComponent(ItemComponent.class);
                            if (item != null) {
                                takeAmount((item.stackCount + 1) / 2);
                            }
                        }
                    }
                }
            }
        }
    };

    private void swapItem() {
        SlotBasedInventoryManager inventoryManager = CoreRegistry.get(SlotBasedInventoryManager.class);
        if (getTransferItem().exists()) {
            setTransferItem(inventoryManager.putItemInSlot(ownerEntity, slot, getTransferItem()));
        } else {
            setTransferItem(inventoryManager.getItemInSlot(ownerEntity, slot));
        }
    }

    private void giveAmount(int amount) {
        SlotBasedInventoryManager inventoryManager = CoreRegistry.get(SlotBasedInventoryManager.class);
        if (inventoryManager.putAmountInSlot(ownerEntity, slot, getTransferItem(), amount)) {
            setTransferItem(EntityRef.NULL);
        }
    }

    private void takeAmount(int amount) {
        SlotBasedInventoryManager inventoryManager = CoreRegistry.get(SlotBasedInventoryManager.class);
        EntityRef item = inventoryManager.getItemInSlot(ownerEntity, slot);
        ItemComponent itemComp = item.getComponent(ItemComponent.class);
        if (itemComp == null) {
            return;
        }
        ItemComponent transferItemComp = getTransferItem().getComponent(ItemComponent.class);
        if (transferItemComp == null) {
            if (itemComp.stackId.isEmpty() || amount >= itemComp.stackCount) {
                inventoryManager.removeItem(ownerEntity, item);
                setTransferItem(item);
            } else {
                EntityRef newItem = CoreRegistry.get(EntityManager.class).copy(item);
                ItemComponent newItemComponent = newItem.getComponent(ItemComponent.class);
                newItemComponent.stackCount = (byte) amount;
                newItem.saveComponent(newItemComponent);
                itemComp.stackCount -= amount;
                item.saveComponent(itemComp);
                setTransferItem(newItem);
            }
        } else if (inventoryManager.canStackTogether(item, getTransferItem())) {
            int amountToTransfer = Math.min(amount, InventorySystem.MAX_STACK - transferItemComp.stackCount);
            if (amountToTransfer > 0) {
                transferItemComp.stackCount += amountToTransfer;
                itemComp.stackCount -= amountToTransfer;
                getTransferItem().saveComponent(transferItemComp);
                if (itemComp.stackCount == 0) {
                    item.destroy();
                } else {
                    item.saveComponent(itemComp);
                }
            }
        }
    }

    private KeyListener keyListener = new KeyListener() {
        @Override
        public void key(UIDisplayElement element, KeyEvent event) {
            if (event.getKey() == Keyboard.KEY_LSHIFT) {
                instantTransferKeyPressed = event.isDown();
            }

            if (event.getKey() == Keyboard.KEY_LCONTROL) {
                multiplierKeyPressed = event.isDown();
            }
        }
    };

    public UIItemCellIcon getTransferItemIcon() {
        return transferIcon;
    }

    /**
     * Displays a little icon and item count for an item cell.
     *
     * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
     */
    public class UIItemCellIcon extends UIDisplayContainer {
        //entity
        private EntityRef itemEntity = EntityRef.NULL;

        //sub elements
        private final UILabel itemCount;

        //layout
        private Texture terrainTex;
        private final Vector2f itemCountPosition = new Vector2f(26f, 5f);
        private boolean displayItemCount = true;
        private boolean moveWithCursor = false;

        public UIItemCellIcon() {
            terrainTex = Assets.getTexture("engine:terrain");

            itemCount = new UILabel();
            itemCount.setVisible(false);
            itemCount.setPosition(itemCountPosition);

            addDisplayElement(itemCount);
        }

        public void setMoveWithCursor(boolean moveWithCursor) {
            this.moveWithCursor = moveWithCursor;
        }

        @Override
        public void layout() {

        }

        @Override
        public void update() {
            if (moveWithCursor) {
                setPosition(new Vector2f(Mouse.getX() - getSize().x / 2, Display.getHeight() - Mouse.getY() - getSize().y / 2));
            }
            ItemComponent itemComponent = itemEntity.getComponent(ItemComponent.class);
            //item count visibility
            if (itemComponent != null) {
                if (itemComponent.stackCount > 1 && displayItemCount) {
                    itemCount.setVisible(true);
                    itemCount.setText(Integer.toString(itemComponent.stackCount));
                } else {
                    itemCount.setVisible(false);
                }
            }
        }

        @Override
        public void render() {
            ItemComponent itemComponent = itemEntity.getComponent(ItemComponent.class);
            if (itemComponent == null)
                return;

            //render icon
            if (itemComponent.icon.isEmpty()) {
                BlockItemComponent blockItem = itemEntity.getComponent(BlockItemComponent.class);
                if (blockItem != null) {
                    renderBlockIcon(blockItem.blockFamily);
                }
            } else {
                Icon icon = Icon.get(itemComponent.icon);
                if (icon != null) {
                    renderIcon(icon);
                }
            }

            super.render();
        }

        private void renderIcon(Icon icon) {
            glEnable(GL11.GL_DEPTH_TEST);
            glClear(GL11.GL_DEPTH_BUFFER_BIT);
            glPushMatrix();
            glTranslatef(20f, 20f, 0f);
            icon.render();
            glPopMatrix();
            glDisable(GL11.GL_DEPTH_TEST);
        }

        private void renderBlockIcon(BlockFamily blockFamily) {
            if (blockFamily == null) {
                renderIcon(Icon.get("questionmark"));
                return;
            }

            glEnable(GL11.GL_DEPTH_TEST);
            glClear(GL11.GL_DEPTH_BUFFER_BIT);
            glPushMatrix();

            glTranslatef(20f, 20f, 0f);

            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glPushMatrix();
            glTranslatef(4f, 0f, 0f);
            GL11.glScalef(20f, 20f, 20f);
            GL11.glRotatef(170f, 1f, 0f, 0f);
            GL11.glRotatef(-16f, 0f, 1f, 0f);
            glBindTexture(GL11.GL_TEXTURE_2D, terrainTex.getId());

            Block block = blockFamily.getArchetypeBlock();
            block.renderWithLightValue(1.0f);

            GL11.glPopMatrix();

            GL11.glDisable(GL11.GL_TEXTURE_2D);
            glPopMatrix();
            glDisable(GL11.GL_DEPTH_TEST);
        }

        public EntityRef getItemEntity() {
            return itemEntity;
        }

        public void setItemEntity(EntityRef itemEntity) {
            this.itemEntity = itemEntity;
        }

        public boolean isDisplayItemCount() {
            return displayItemCount;
        }

        public void setDisplayItemCount(boolean enable) {
            displayItemCount = enable;
        }
    }

    public UIItemCell(EntityRef owner, Vector2f size) {
        this(owner, size, iconPosition);
    }

    /**
     * Create a single item cell which is capable of holding an item.
     *
     * @param owner        The owner of this item.
     * @param size         The size of the icon cell.
     * @param iconPosition The position of the icon cell.
     */
    public UIItemCell(EntityRef owner, Vector2f size, Vector2f iconPosition) {
        this.ownerEntity = owner;

        setSize(size);

        Texture guiTex = Assets.getTexture("engine:gui");

        selectionRectangle = new UIImage(guiTex);
        selectionRectangle.setTextureSize(new Vector2f(22f, 22f));
        selectionRectangle.setTextureOrigin(new Vector2f(1f, 23f));
        selectionRectangle.setSize(new Vector2f(getSize().x, getSize().y));

        background = new UIImage(Assets.getTexture("engine:inventory"));
        background.setTextureSize(new Vector2f(19f, 19f));
        background.setTextureOrigin(new Vector2f(3f, 146f));
        background.setSize(getSize());
        background.setVisible(true);
        background.setFixed(true);

        itemLabel = new UILabel();
        itemLabel.setVisible(false);
        itemLabel.setPosition(itemLabelPosition);

        icon = new UIItemCellIcon();
        icon.setPosition(iconPosition);
        icon.setVisible(true);

        if (transferIcon == null) {
            transferIcon = new UIItemCellIcon();
            transferIcon.setVisible(false);
            transferIcon.setItemEntity(EntityRef.NULL);
            transferIcon.moveWithCursor = true;
        }

        addMouseMoveListener(mouseMoveListener);
        addMouseButtonListener(mouseButtonListener);
        addKeyListener(keyListener);

        addDisplayElement(background);
        addDisplayElement(icon);
        addDisplayElement(selectionRectangle);
        addDisplayElement(itemLabel);
    }

    /**
     * Update the transfer icon position to the current mouse position.
     * TODO all item cells with the same ownerEntity are updating the position -> just the dragged (source) item cell should update the position
     */

    /**
     * Set the visibility of the label.
     *
     * @param enable True to be displayed.
     */
    private void setLabelVisibility(boolean enable) {
        if (itemEntity.exists()) {
            ItemComponent item = itemEntity.getComponent(ItemComponent.class);
            BlockItemComponent blockItem = itemEntity.getComponent(BlockItemComponent.class);

            if (item != null) {
                if (blockItem != null) {
                    if (blockItem.blockFamily != null) {
                        itemLabel.setText(blockItem.blockFamily.getDisplayName());
                    } else {
                        itemLabel.setText("Broken Block");
                    }

                } else {
                    itemLabel.setText(item.name);
                }
            }

            itemLabel.setVisible(enable);
        } else {
            itemLabel.setVisible(false);
        }
    }

    private boolean canPartialMoveTo() {
        SlotBasedInventoryManager inventoryManager = CoreRegistry.get(SlotBasedInventoryManager.class);
        EntityRef targetItem = inventoryManager.getItemInSlot(ownerEntity, slot);
        if (inventoryManager.canStackTogether(getTransferItem(), targetItem)) {
            return true;
        }

        return !targetItem.exists();
    }

    /**
     * Move the item in the transfer inventory slot to this owners inventory automatically to a free slot or merge if an item of the same block exists. The item will be dropped if no free slot is available.
     */
    private void moveItemDirect() {
        SlotBasedInventoryManager inventoryManager = CoreRegistry.get(SlotBasedInventoryManager.class);
        inventoryManager.giveItem(connectedEntity, inventoryManager.getItemInSlot(ownerEntity, slot));
    }

    /**
     * Get item from the transfer slot.
     *
     * @return Returns the item in the transfer slot.
     */
    private EntityRef getTransferItem() {
        return CoreRegistry.get(LocalPlayer.class).getCharacterEntity().getComponent(CharacterComponent.class).transferSlot;
    }

    private void setTransferItem(EntityRef item) {
        ItemComponent itemComp = item.getComponent(ItemComponent.class);
        if (itemComp != null && itemComp.container.exists()) {
            CoreRegistry.get(InventoryManager.class).removeItem(itemComp.container, item);
        }
        EntityRef character = CoreRegistry.get(LocalPlayer.class).getCharacterEntity();
        CharacterComponent comp = character.getComponent(CharacterComponent.class);
        comp.transferSlot = item;
        character.saveComponent(comp);
        if (item.exists()) {
            transferIcon.setItemEntity(item);
            getGUIManager().getFocusedWindow().removeDisplayElement(transferIcon);
            getGUIManager().getFocusedWindow().addDisplayElement(transferIcon);
            transferIcon.setVisible(true);
        } else {
            transferIcon.setItemEntity(EntityRef.NULL);
            transferIcon.setVisible(false);
            getGUIManager().getFocusedWindow().removeDisplayElement(transferIcon);
        }
    }




    /**
     * Set the item which this item cell contains.
     *
     * @param itemEntity The item.
     * @param slot       The slot number in the inventory of the owner.
     */
    public void setItemEntity(EntityRef itemEntity, int slot) {
        this.itemEntity = itemEntity;
        icon.setItemEntity(itemEntity);
        this.slot = slot;
    }

    /**
     * Get the item which this item cell contains.
     *
     * @return Returns the item entity.
     */
    public EntityRef getItemEntity() {
        return itemEntity;
    }

    /**
     * Check if the cell supports drag/drop.
     *
     * @return True if the cell supports drag/drop.
     */
    public boolean isDrag() {
        return enableDrag;
    }

    /**
     * Set the support for drag/drop to this cell.
     *
     * @param enable True to enable drag/drop.
     */
    public void setDrag(boolean enable) {
        this.enableDrag = enable;
    }

    /**
     * Check if the cell has an selection rectangle as the mouse is over.
     *
     * @return True if the cell has an selection rectangle as the mouse is over.
     */
    public boolean isDisplaySelection() {
        return enableSelectionRectangle;
    }

    /**
     * Set if the cell will show a selection rectangle as the mouse is over.
     *
     * @param enable True to enable the selection rectangle as the mouse is over.
     */
    public void setDisplaySelection(boolean enable) {
        this.enableSelectionRectangle = enable;
    }

    /**
     * Check if the cell shows an item count.
     *
     * @return Returns true if the cell shows an item count.
     */
    public boolean isDisplayItemCount() {
        return icon.isDisplayItemCount();
    }

    /**
     * Set if the cell shows an item count.
     *
     * @param enable True to display an item count.
     */
    public void setDisplayItemCount(boolean enable) {
        icon.setDisplayItemCount(enable);
    }

    /**
     * Check whether the selection rectangle is shown.
     *
     * @return Returns true if the selection rectangle is shown.
     */
    public boolean getSelection() {
        return selectionRectangle.isVisible();
    }

    /**
     * Set the visibility of the selection rectangle.
     *
     * @param enable True to enable the selection rectangle.
     */
    public void setSelection(boolean enable) {
        if (enableSelectionRectangle) {
            selectionRectangle.setVisible(enable);
        }
    }

    /**
     * Get the inventory entity which is connected with this cell. This allows fast transfer between 2 inventories.
     *
     * @return Returns the entity of the connected inventory.
     */
    public EntityRef getConnected() {
        return connectedEntity;
    }

    /**
     * Set the inventory entity which is connected with this cell. This allows fast transfer between 2 inventories.
     *
     * @param entity The entity inventory to connect with this cell.
     */
    public void setConnected(EntityRef entity) {
        this.connectedEntity = entity;
    }
}

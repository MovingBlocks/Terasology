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
package org.terasology.rendering.gui.components;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.terasology.componentSystem.items.InventorySystem;
import org.terasology.components.InventoryComponent;
import org.terasology.components.ItemComponent;
import org.terasology.components.PlayerComponent;
import org.terasology.components.block.BlockItemComponent;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.game.CoreRegistry;
import org.terasology.asset.AssetManager;
import org.terasology.logic.LocalPlayer;
import org.terasology.logic.manager.GUIManager;
import org.terasology.rendering.assets.Texture;
import org.terasology.rendering.gui.framework.UIDisplayContainer;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.UIGraphicsElement;
import org.terasology.rendering.gui.framework.events.MouseButtonListener;
import org.terasology.rendering.gui.framework.events.MouseMoveListener;

import javax.vecmath.Vector2f;

/**
 * A cell which can contain an item and supports drag and drop.
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 *
 * TODO empty the movement cell as the window was closed.
 * TODO save the cell which was dragged.
 */
public class UIItemCell extends UIDisplayContainer  {

    //movement
    private static UIItemCellIcon movementIcon;
    
    //entity
    private EntityRef ownerEntity;
    private EntityRef itemEntity;
    private int slot;
    
    //sub elements
    private final UIGraphicsElement selectionRectangle;
    private final UIGraphicsElement background;
    private final UIText itemLabel;
    private UIItemCellIcon icon;
    
    //layout
    private Vector2f itemLabelPosition = new Vector2f(0f, -14f);
    
    //settings
    private boolean enableDrag = true;
    private boolean enableSelectionRectangle = true;

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
            InventoryComponent ownerInventory = ownerEntity.getComponent(InventoryComponent.class);
            if (ownerInventory != null) {
                if (getFromMovementSlot().exists()) { //TODO avoid to let EVERY cell update the position
                    movementIcon.setPosition(new Vector2f(Mouse.getX() - getSize().x / 2, Display.getHeight() - Mouse.getY() - getSize().y / 2));
                }
            }
        }
    };
    private MouseButtonListener mouseButtonListener = new MouseButtonListener() {        
        @Override
        public void wheel(UIDisplayElement element, int wheel, boolean intersect) {
            
        }
        
        @Override
        public void up(UIDisplayElement element, int button, boolean intersect) {
            InventoryComponent ownerInventory = ownerEntity.getComponent(InventoryComponent.class);
            if (intersect) {
                if (!enableDrag)
                    return;
                
                if (ownerInventory != null) {
                    if (button == 0) {
                        //drop
                        if (getFromMovementSlot().exists()) {
                            
                            moveItem();
                            
                        }
                        //drag
                        else {
                            
                            //move item to the movement slot
                            sendToMovementSlot(itemEntity, (byte) -1);
                            
                            //enable movement icon
                            setMovementIconVisibility(true);
                            
                        }
                    }
                    else if (button == 1) {
                        if (!getFromMovementSlot().exists()) {
                            
                            //copy half of the stack
                            ItemComponent item = itemEntity.getComponent(ItemComponent.class);
                            sendToMovementSlot(itemEntity, (byte) (item.stackCount / 2));
                            
                            //enable movement icon
                            setMovementIconVisibility(true);
                            
                        }
                        else {
                            
                        }
                    }
                }
            }
            else {
                //TODO reset dragged item if not dropped onto a UIItemCell. (how can we figure this out..)
            }
        }
        
        @Override
        public void down(UIDisplayElement element, int button, boolean intersect) {
            //TODO do the stuff on mouse button down. but there is probably a bug in the GUI event system? event will be send twice?
        }
    };
        
    /**
     * Create a single item cell which is capable of holding an item.
     * @param owner The owner of this item.
     * @param size The size of the icon cell.
     */
    public UIItemCell(EntityRef owner, Vector2f size) {
        this.ownerEntity = owner;
        
        setSize(size);

        Texture guiTex = AssetManager.loadTexture("engine:gui");
        
        selectionRectangle = new UIGraphicsElement(guiTex);
        selectionRectangle.getTextureSize().set(new Vector2f(24f / 256f, 24f / 256f));
        selectionRectangle.getTextureOrigin().set(new Vector2f(0.0f, 23f / 256f));
        selectionRectangle.setSize(getSize());

        background = new UIGraphicsElement(guiTex);
        background.getTextureSize().set(new Vector2f(20f / 256f, 20f / 256f));
        background.getTextureOrigin().set(new Vector2f(1.0f / 256f, 1f / 256f));
        background.setSize(getSize());
        background.setVisible(true);
        background.setFixed(true);

        itemLabel = new UIText();
        itemLabel.setVisible(false);
        itemLabel.setPosition(itemLabelPosition);
                
        icon = new UIItemCellIcon();
        icon.setVisible(true);
        
        if (movementIcon == null) {
            movementIcon = new UIItemCellIcon();
            movementIcon.setVisible(false);
            movementIcon.setItemEntity(EntityRef.NULL);
        }
        
        addMouseMoveListener(mouseMoveListener);
        addMouseButtonListener(mouseButtonListener);
        
        addDisplayElement(background);
        addDisplayElement(icon);
        addDisplayElement(selectionRectangle);
        addDisplayElement(itemLabel);
    }
    
    /**
     * Move the item in the movement inventory slot to this owners inventory slot.
     */
    private void moveItem() {
        EntityRef item = getFromMovementSlot();
        InventoryComponent ownerInventory = ownerEntity.getComponent(InventoryComponent.class);
        
        //no items on the target slot
        if (!ownerInventory.itemSlots.get(slot).exists()) {
            moveItemPlace();
        }
        //target slot contains an item
        else {
            ItemComponent sourceItem = item.getComponent(ItemComponent.class);
            ItemComponent targetItem = ownerInventory.itemSlots.get(slot).getComponent(ItemComponent.class);

            //target slot contains same item and is stackable
            if (targetItem.stackId.equals(sourceItem.stackId) && !targetItem.stackId.isEmpty() && !sourceItem.stackId.isEmpty()) {
                moveItemMerge();
            }
            //target slot contains another item and/or none stackable
            else {
                moveItemSwap();
            }
            
        }
        
        //notify component changed listeners
        ownerEntity.saveComponent(ownerInventory);
    }

    /**
     * Place the movement item directly on this owners inventory slot.
     */
    private void moveItemPlace() {
        InventoryComponent ownerInventory = ownerEntity.getComponent(InventoryComponent.class);
        EntityRef item = getFromMovementSlot();
        ItemComponent sourceItem = item.getComponent(ItemComponent.class);

        //place the item in the item slot
        sourceItem.container = ownerEntity;
        ownerInventory.itemSlots.set(slot, item);
        
        //remove item from movement slot
        sendToMovementSlot(EntityRef.NULL, (byte) -1);
        
        //disable movement icon
        setMovementIconVisibility(false);
    }

    /**
     * Merge the movement item with this owners inventory slot.
     */
    private void moveItemMerge() {
        EntityRef item = getFromMovementSlot();
        InventoryComponent ownerInventory = ownerEntity.getComponent(InventoryComponent.class);
        ItemComponent sourceItem = item.getComponent(ItemComponent.class);
        ItemComponent targetItem = ownerInventory.itemSlots.get(slot).getComponent(ItemComponent.class);
        
        int spaceLeft = InventorySystem.MAX_STACK - targetItem.stackCount;
        
        //source stack is to big to merge full in
        if (spaceLeft < sourceItem.stackCount) {
            
            targetItem.stackCount = InventorySystem.MAX_STACK;
            sourceItem.stackCount -= spaceLeft;
            
        }
        //merge source stack fully in
        else {
            
            targetItem.stackCount += sourceItem.stackCount;
            
            //remove item from movement slot
            sendToMovementSlot(EntityRef.NULL, (byte) -1);
            
            //disable movement icon
            setMovementIconVisibility(false);
            
        }
    }
    
    /**
     * Swap the items of this owners inventory slot and the movement item slot.
     */
    private void moveItemSwap() {
        EntityRef item = getFromMovementSlot();
        InventoryComponent ownerInventory = ownerEntity.getComponent(InventoryComponent.class);
        ItemComponent sourceItem = item.getComponent(ItemComponent.class);
        
        //move item to the movement slot
        sendToMovementSlot(itemEntity, (byte) -1);
        
        //place the item in the item slot
        sourceItem.container = ownerEntity;
        ownerInventory.itemSlots.set(slot, item);
        
        //enable movement icon
        setMovementIconVisibility(true);
    }
    
    /**
     * Resets the item in the movement slot to its owner.
     * TODO ...
     */
    private void reset() {
        InventoryComponent ownerInventory = ownerEntity.getComponent(InventoryComponent.class);
        ownerInventory.itemSlots.set(slot, getFromMovementSlot());
        sendToMovementSlot(EntityRef.NULL, (byte) -1);
    }
    
    /**
     * Get item in the movement slot.
     * @return Returns the item in the movement slot.
     */
    private EntityRef getFromMovementSlot() {
        return CoreRegistry.get(LocalPlayer.class).getEntity().getComponent(PlayerComponent.class).movementSlot;
    }
    
    /**
     * Send an item in the movement slot.
     * @param item The item to send to the movement slot.
     * @param amount The amount to send to the movement slot. -1 for whole stack.
     */
    private void sendToMovementSlot(EntityRef item, byte amount) {
        InventoryComponent ownerInventory = ownerEntity.getComponent(InventoryComponent.class);
        //transfer whole stack
        if (amount == -1) {
            
            CoreRegistry.get(LocalPlayer.class).getEntity().getComponent(PlayerComponent.class).movementSlot = item;
            
            if (item.exists()) {
                //remove the item from the inventory slot
                ownerInventory.itemSlots.set(ownerInventory.itemSlots.indexOf(item), EntityRef.NULL);
            }
            
        }
        //transfer part of stack
        else if (amount > 0 && amount <= InventorySystem.MAX_STACK) {
            ItemComponent itemComponent = itemEntity.getComponent(ItemComponent.class);
            itemComponent.stackCount -= amount;
            
            //create the item
            EntityManager entityManager = CoreRegistry.get(EntityManager.class);
            EntityRef moveItem = entityManager.copy(item);
            itemComponent = moveItem.getComponent(ItemComponent.class);
            itemComponent.stackCount = amount;
            itemComponent.container = ownerEntity;
            
            CoreRegistry.get(LocalPlayer.class).getEntity().getComponent(PlayerComponent.class).movementSlot = moveItem;
        }
        
        //notify component changed listeners
        ownerEntity.saveComponent(ownerInventory);
    }
    
    /**
     * Set the visibility of the movement icon.
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
    
    /**
     * Set the visibility of the movement icon.
     * @param enable True to be displayed.
     */
    private void setMovementIconVisibility(boolean enable) {
        if (enable) {
            GUIManager.getInstance().getFocusedWindow().addDisplayElement(movementIcon);
            movementIcon.setItemEntity(getFromMovementSlot());
        } else {
            GUIManager.getInstance().getFocusedWindow().removeDisplayElement(movementIcon);
            movementIcon.setItemEntity(EntityRef.NULL);
        }
        
        movementIcon.setVisible(enable);
    }
    
    /**
     * Set the item which this item cell contains.
     * @param itemEntity The item.
     * @param slot The slot number in the inventory of the owner.
     */
    public void setItem(EntityRef itemEntity, int slot) {
        this.itemEntity = itemEntity;
        icon.setItemEntity(itemEntity);
        this.slot = slot;
    }
    
    /**
     * Get the owner of this cell.
     * @return The owner entity.
     */
    public EntityRef getOwnerEntity() {
        return ownerEntity;
    }
    
    /**
     * Get the item which this item cell contains.
     * @return Returns the item entity.
     */
    public EntityRef getItemEntity() {
        return itemEntity;
    }

    /**
     * Check if the cell supports drag/drop.
     * @return True if the cell supports drag/drop.
     */
    public boolean isDrag() {
        return enableDrag;
    }

    /**
     * Set the support for drag/drop to this cell.
     * @param enable True to enable drag/drop.
     */
    public void setDrag(boolean enable) {
        this.enableDrag = enable;
    }
    
    /**
     * Check if the cell has an selection rectangle as the mouse is over.
     * @return True if the cell has an selection rectangle as the mouse is over.
     */
    public boolean isSelectionRectangle() {
        return enableSelectionRectangle;
    }

    /**
     * Set if the cell will show a selection rectangle as the mouse is over.
     * @param enable True to enable the selection rectangle as the mouse is over.
     */
    public void setSelectionRectangle(boolean enable) {
        this.enableSelectionRectangle = enable;
    }
    
    /**
     * Set the visibility of the selection rectangle.
     * @param enable True to enable the selection rectangle.
     */
    public void setSelectionRectangleEnable(boolean enable) {
        selectionRectangle.setVisible(enable);
    }
}

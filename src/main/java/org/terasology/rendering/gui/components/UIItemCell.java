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

import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.terasology.asset.AssetManager;
import org.terasology.componentSystem.items.InventorySystem;
import org.terasology.components.InventoryComponent;
import org.terasology.components.ItemComponent;
import org.terasology.components.LocalPlayerComponent;
import org.terasology.components.PlayerComponent;
import org.terasology.components.block.BlockItemComponent;
import org.terasology.entityFactory.DroppedBlockFactory;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.events.input.binds.RunButton;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.LocalPlayer;
import org.terasology.logic.manager.GUIManager;
import org.terasology.physics.ImpulseEvent;
import org.terasology.rendering.assets.Texture;
import org.terasology.rendering.gui.framework.UIDisplayContainer;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.UIDisplayWindow;
import org.terasology.rendering.gui.framework.UIGraphicsElement;
import org.terasology.rendering.gui.framework.events.MouseButtonListener;
import org.terasology.rendering.gui.framework.events.MouseMoveListener;
import org.terasology.rendering.gui.framework.events.WindowListener;

/**
 * A cell which can contain an item and supports drag and drop.
 * To move an item the item will be moved to a special transfer slot as item will be dragged. This slot is in the PlayerComponent class.
 * Therefore the item belongs to nobody as the transfer is ongoing and needs to be reseted as the action was interrupted.
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 * @see PlayerComponent
 */
public class UIItemCell extends UIDisplayContainer  {

    //transfer
    private static UIItemCellIcon transferIcon;
    
    //entity
    private EntityRef ownerEntity = EntityRef.NULL;
    private EntityRef itemEntity = EntityRef.NULL;
    private int slot;
    
    //connected inventory entity
    private EntityRef connectedEntity = EntityRef.NULL;
    private boolean fastTransferPressed = false;
    
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
                if (getFromTransferSlot().exists()) {
                    ItemComponent item = getFromTransferSlot().getComponent(ItemComponent.class);
                    if (item.container == ownerEntity) {
                        transferIcon.setPosition(new Vector2f(Mouse.getX() - getSize().x / 2, Display.getHeight() - Mouse.getY() - getSize().y / 2));
                    }
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

        }
        
        @Override
        public void down(UIDisplayElement element, int button, boolean intersect) {
            InventoryComponent ownerInventory = ownerEntity.getComponent(InventoryComponent.class);
            if (intersect) {
                if (!enableDrag)
                    return;
                
                if (ownerInventory != null) {
                    //left click
                    if (button == 0) {
                        //drop
                        if (getFromTransferSlot().exists()) {
                            
                            moveItem(UIItemCell.this, (byte) 0, true);
                            
                        }
                        //drag
                        else {
                            
                            //move item to the transfer slot
                            sendToTransferSlot(itemEntity, (byte) 0);
                            
                            if (fastTransferPressed && connectedEntity.exists()) {
                                moveItemAutomatic(connectedEntity, true);
                            }
                        }
                    }
                    //right click
                    else if (button == 1) {
                        //drop
                        if (getFromTransferSlot().exists()) {
                         
                            moveItem(UIItemCell.this, (byte) 1, true);
                            
                        }
                        //drag
                        else {
                            
                            //copy half of the stack
                            ItemComponent item = itemEntity.getComponent(ItemComponent.class);
                            if (item != null) {
                                sendToTransferSlot(itemEntity, (byte) (item.stackCount / 2));
                            }
                            
                        }
                    }
                }
            }
            else {
                //TODO reset dragged item if not dropped onto a UIItemCell. (how can we figure this out..)
            }
        }
    };
    private WindowListener windowListener = new WindowListener() {
        @Override
        public void open(UIDisplayElement element) {
            
        }
        
        @Override
        public void close(UIDisplayElement element) {
            UIDisplayWindow window = (UIDisplayWindow) element;
            
            //TODO just drop the item?
            //lets reset the item of the window got closed.
            reset();
            
            window.removeWindowListener(this);
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
        
        if (transferIcon == null) {
            transferIcon = new UIItemCellIcon();
            transferIcon.setVisible(false);
            transferIcon.setItemEntity(EntityRef.NULL);
        }
        
        addMouseMoveListener(mouseMoveListener);
        addMouseButtonListener(mouseButtonListener);
        
        addDisplayElement(background);
        addDisplayElement(icon);
        addDisplayElement(selectionRectangle);
        addDisplayElement(itemLabel);
    }
    

    /**
     * Drop the item in the transfer slot.
     * TODO not working yet.
     */
    private void dropitem() {
        EntityRef item = getFromTransferSlot();
        
        if (item.exists()) {
            BlockItemComponent blockItem = item.getComponent(BlockItemComponent.class);
            
            int dropPower = 6;
            EntityManager entityManager = CoreRegistry.get(EntityManager.class);
            LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);
            LocalPlayerComponent localPlayerComp = localPlayer.getEntity().getComponent(LocalPlayerComponent.class);
            DroppedBlockFactory droppedBlockFactory = new DroppedBlockFactory(entityManager);
            EntityRef droppedBlock = droppedBlockFactory.newInstance(new Vector3f(localPlayer.getPosition().x + localPlayer.getViewDirection().x * 1.5f, localPlayer.getPosition().y + localPlayer.getViewDirection().y * 1.5f, localPlayer.getPosition().z + localPlayer.getViewDirection().z * 1.5f), blockItem.blockFamily, 20);
            droppedBlock.send(new ImpulseEvent(new Vector3f(localPlayer.getViewDirection().x*dropPower, localPlayer.getViewDirection().y*dropPower, localPlayer.getViewDirection().z*dropPower)));
            
            localPlayerComp.handAnimation = 0.5f;
        }
        
        System.out.println("drop");
    }
    
    /**
     * Move the item in the transfer inventory slot to this owners inventory automatically to a free slot or merge if an item of the same block exists. The item will be dropped if no free slot is available.
     * @param targetEntity The target inventory entity to send the item too.
     * @param dropOnFull True if drop the item if target inventory is full.
     */
    private void moveItemAutomatic(EntityRef targetEntity, boolean dropOnFull) {
        ItemComponent sourceItem = getFromTransferSlot().getComponent(ItemComponent.class);
        ItemComponent targetItem;
        
        InventoryComponent targetInventory = targetEntity.getComponent(InventoryComponent.class);
        
        if (getFromTransferSlot().exists() && targetInventory != null) {
            
            //is stackable
            if (!sourceItem.stackId.isEmpty()) {
                for (EntityRef itemStack : targetInventory.itemSlots) {
                    targetItem = itemStack.getComponent(ItemComponent.class);
                    
                    if (targetItem != null) {
                        if (targetItem.stackId.equals(sourceItem.stackId)) {
                            
                            int spaceLeft = InventorySystem.MAX_STACK - targetItem.stackCount;
                            
                            //source stack is to big to merge full in
                            if (spaceLeft < sourceItem.stackCount) {
                                
                                targetItem.stackCount = InventorySystem.MAX_STACK;
                                sourceItem.stackCount -= spaceLeft;
                                
                            }
                            //merge source stack fully in
                            else {
                                
                                targetItem.stackCount += sourceItem.stackCount;
                                
                                //remove item from transfer slot
                                sendToTransferSlot(EntityRef.NULL, (byte) 0);
                                
                            }
                            
                        }
                    }
                    
                    //check if stack is fully merged into existing stacks
                    if (!getFromTransferSlot().exists()) {
                        break;
                    }
                    
                }
            }
            
            //check if after merging (or not able to merge) still items left (this can only happen if the player receives an item in the meantime)
            if (getFromTransferSlot().exists()) {
                
                //check for a free slot
                int freeSlot = targetInventory.itemSlots.indexOf(EntityRef.NULL);
                if (freeSlot != -1) {
                    //place the item in the item slot
                    sourceItem.container = targetEntity;
                    targetInventory.itemSlots.set(freeSlot, getFromTransferSlot());
                    
                    //remove item from transfer slot
                    sendToTransferSlot(EntityRef.NULL, (byte) 0);
                }
                //no free slots
                else {
                    if (dropOnFull)
                        dropitem();
                }
                
            }
            
            //notify component changed listeners
            targetEntity.saveComponent(targetInventory);
            
        }
    }
    
   /**
    * Move the item in the transfer inventory slot to the targetCell.
    * This method is split up into moveItemPlace, moveItemMerge and moveItemSwap.
    * @param targetCell The target cell to move the item to.
    * @param amount The amount to move.
    * @param dropOnFail Drop the item on failure.
    */
    private void moveItem(UIItemCell targetCell, byte amount, boolean dropOnFail) {
        EntityRef item = getFromTransferSlot();
        InventoryComponent ownerInventory = ownerEntity.getComponent(InventoryComponent.class);
        if (item.exists()) {

            boolean success = false;
            
            if (!success) {
                //try to place the block
                success = moveItemPlace(targetCell, amount);
            }
            
            if (!success) {
                //try to merge
                success = moveItemMerge(targetCell, amount);
            }
            
            if (!success) {
                //try to swap
                success = moveItemSwap(targetCell);
            }
            
            if (!success && dropOnFail) {
                //failed (should not happen)
                dropitem();
            }

            //notify component changed listeners
            ownerEntity.saveComponent(ownerInventory);
            
        }
    }

    /**
     * Place the item in the transfer slot directly on the target cell.
     * @param targetCell The target cell to move the item to.
     * @param amount The amount to place. 0 for whole stack.
     */
    private boolean moveItemPlace(UIItemCell targetCell, byte amount) {
        EntityRef item = getFromTransferSlot();
        InventoryComponent ownerInventory = ownerEntity.getComponent(InventoryComponent.class);
        ItemComponent sourceItem = item.getComponent(ItemComponent.class);
        
        //check if target slot is empty
        if (!ownerInventory.itemSlots.get(targetCell.slot).exists()) {    
            //place whole stack
            if (amount == 0) {
                //place the item in the item slot
                sourceItem.container = ownerEntity;
                ownerInventory.itemSlots.set(targetCell.slot, item);
                
                //remove item from transfer slot
                sendToTransferSlot(EntityRef.NULL, (byte) 0);
                
                return true;
            }
            //place an specific amount
            else {
                //create an item
                EntityManager entityManager = CoreRegistry.get(EntityManager.class);
                EntityRef copy = entityManager.copy(item);
                ItemComponent copyItem = copy.getComponent(ItemComponent.class);
    
                //items in transfer slot left
                if (sourceItem.stackCount > amount) {
                    sourceItem.stackCount -= amount;
                    copyItem.stackCount = amount;
                    
                    copyItem.container = ownerEntity;
                    ownerInventory.itemSlots.set(targetCell.slot, copy);
 
                    return true;
                }
                //no items in transfer slot left
                else {
                    //place whole stack
                    ownerInventory.itemSlots.set(targetCell.slot, item);
                    
                    //remove item from transfer slot
                    sendToTransferSlot(EntityRef.NULL, (byte) 0);
                    
                    return true;
                }
            }
        }
        
        return false;
    }

    /**
     * Merge the item in the transfer slot with the target cell.
     * @param targetCell The target cell to move the item to.
     * @param amount The amount to merge. 0 for whole stack.
     */
    private boolean moveItemMerge(UIItemCell targetCell, byte amount) {
        EntityRef item = getFromTransferSlot();
        ItemComponent sourceItem = item.getComponent(ItemComponent.class);
        ItemComponent targetItem = targetCell.getOwnerEntity().getComponent(InventoryComponent.class).itemSlots.get(targetCell.slot).getComponent(ItemComponent.class);
        
        //make sure the items can be merged
        if (targetItem.stackId.equals(sourceItem.stackId) && !targetItem.stackId.isEmpty() && !sourceItem.stackId.isEmpty()) {
            //merge whole stack
            if (amount == 0) {
                int spaceLeft = InventorySystem.MAX_STACK - targetItem.stackCount;
                
                //source stack is to big to merge full in
                if (spaceLeft < sourceItem.stackCount) {
                    
                    targetItem.stackCount = InventorySystem.MAX_STACK;
                    sourceItem.stackCount -= spaceLeft;
                    
                    return true;
                }
                //merge source stack fully in
                else {
                    
                    targetItem.stackCount += sourceItem.stackCount;
                    
                    //remove item from transfer slot
                    sendToTransferSlot(EntityRef.NULL, (byte) 0);
                    
                    return true;
                }
            }
            //merge an specific amount
            else {
                int spaceLeft = InventorySystem.MAX_STACK - targetItem.stackCount;
                
                //items can be merged in
                if (spaceLeft > amount) {
                    targetItem.stackCount += amount;
                    
                    //items left in transfer slot left
                    if (sourceItem.stackCount > amount) {
                        //subtract the transfered amount
                        sourceItem.stackCount -= amount;
                    }
                    //no items in transfer slot left
                    else {
                        //remove item from transfer slot
                        sendToTransferSlot(EntityRef.NULL, (byte) 0);
                    }

                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Swap the items of the transfer item slot and the target cell.
     * @param targetCell The target cell to move the item to.
     */
    private boolean moveItemSwap(UIItemCell targetCell) {
        EntityRef item = getFromTransferSlot();
        InventoryComponent ownerInventory = ownerEntity.getComponent(InventoryComponent.class);
        ItemComponent sourceItem = item.getComponent(ItemComponent.class);
        ItemComponent targetItem = targetCell.itemEntity.getComponent(ItemComponent.class);
        
        //move item to the transfer slot
        targetItem.container = sourceItem.container;
        sendToTransferSlot(targetCell.itemEntity, (byte) 0);
        
        //place the item in the item slot
        sourceItem.container = ownerEntity;
        ownerInventory.itemSlots.set(targetCell.slot, item);
        
        return true;
    }
    
    /**
     * Resets the item in the transfer slot to its owner.
     */
    private void reset() {
        EntityRef item = getFromTransferSlot();
        if (item.exists()) {
            ItemComponent itemComponent = item.getComponent(ItemComponent.class);
            moveItemAutomatic(itemComponent.container, true);
        }
    }
    
    @Override
    public boolean processBindButton(String id, boolean pressed) {
        if (id.equals(RunButton.ID)) {
            fastTransferPressed = pressed;
            
            return true;
        }
        
        return super.processBindButton(id, pressed);
    }
    
    /**
     * Get item from the transfer slot.
     * @return Returns the item in the transfer slot.
     */
    private EntityRef getFromTransferSlot() {
        return CoreRegistry.get(LocalPlayer.class).getEntity().getComponent(PlayerComponent.class).transferSlot;
    }
    
    /**
     * Send an item to the transfer slot. Update the visibility of the transfer icon.
     * @param item The item to send to the transfer slot.
     * @param amount The amount to send to the transfer slot. 0 for whole stack.
     */
    private void sendToTransferSlot(EntityRef item, byte amount) {
        InventoryComponent ownerInventory = ownerEntity.getComponent(InventoryComponent.class);
        //transfer whole stack
        if (amount == 0) {
            
            CoreRegistry.get(LocalPlayer.class).getEntity().getComponent(PlayerComponent.class).transferSlot = item;
            
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
            
            CoreRegistry.get(LocalPlayer.class).getEntity().getComponent(PlayerComponent.class).transferSlot = moveItem;
        }
        
        //enable/disable transfer item
        if (getFromTransferSlot().exists()) {
            GUIManager.getInstance().getFocusedWindow().removeDisplayElement(transferIcon);
            GUIManager.getInstance().getFocusedWindow().addDisplayElement(transferIcon, "transferIcon");
            GUIManager.getInstance().getFocusedWindow().removeWindowListener(windowListener);
            GUIManager.getInstance().getFocusedWindow().addWindowListener(windowListener);
            transferIcon.setItemEntity(getFromTransferSlot());
            transferIcon.setVisible(true);
        }
        else {
            GUIManager.getInstance().getFocusedWindow().removeDisplayElement(transferIcon);
            transferIcon.setItemEntity(EntityRef.NULL);
            transferIcon.setVisible(false);
        }
        
        //notify component changed listeners
        ownerEntity.saveComponent(ownerInventory);
    }
    
    /**
     * Set the visibility of the label.
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
    
    /**
     * Get the inventory entity which is connected with this cell. This allows fast transfer between 2 inventories.
     * @return Returns the entity of the connected inventory.
     */
    public EntityRef getConnected() {
        return connectedEntity;
    }

    /**
     * Set the inventory entity which is connected with this cell. This allows fast transfer between 2 inventories.
     * @param entity The entity inventory to connect with this cell.
     */
    public void setConnected(EntityRef entity) {
        this.connectedEntity = entity;
    }
}

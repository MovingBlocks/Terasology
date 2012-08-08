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
import org.terasology.components.ItemComponent;
import org.terasology.components.block.BlockItemComponent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.asset.AssetManager;
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
 */
public class UIItemCell extends UIDisplayContainer  {

	//movement   
    private static InventoryMovementEntity movementInventory = new InventoryMovementEntity();

    //entity
    private EntityRef ownerEntity;
    private EntityRef itemEntity;
    
	//sub elements
    private final UIGraphicsElement selectionRectangle;
    private final UIGraphicsElement background;
    private final UIText itemLabel;
    private static UIItemCellIcon movementIcon;
	private UIItemCellIcon icon;
    
    //layout
	private Vector2f itemLabelPosition = new Vector2f(0f, -14f);
	
	//settings
	private boolean enableDrag = true;
	private boolean enableSelectionRectangle = true;

	private MouseMoveListener mouseMoveListener = new MouseMoveListener() {	
		@Override
		public void leave(UIDisplayElement element) {
			if (enableSelectionRectangle)
				selectionRectangle.setVisible(false);
            itemLabel.setVisible(false);
		}
		
		@Override
		public void hover(UIDisplayElement element) {

		}
		
		@Override
		public void enter(UIDisplayElement element) {
	    	if (itemEntity != null) {
		        ItemComponent item = itemEntity.getComponent(ItemComponent.class);
		        BlockItemComponent blockItem = itemEntity.getComponent(BlockItemComponent.class);
		        
		        if (item != null) {	
			        if (blockItem != null)
			        	itemLabel.setText(blockItem.blockFamily.getTitle());
			        else
			        	itemLabel.setText(item.name);
		        }
	    	}
	    	
	    	if (enableSelectionRectangle)
	    		selectionRectangle.setVisible(true);
            itemLabel.setVisible(true);
		}

		@Override
		public void move(UIDisplayElement element) {
			if (movementInventory.hasitem()) {
				if (movementIcon.getItemEntity() == null) {
					movementIcon.setItemEntity(movementInventory.getItem());
				}

				if (movementInventory.getItem() == movementIcon.getItemEntity()) {
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
			if (intersect) {
				if (!enableDrag)
					return;
				
				if (button == 0) {
					if (movementInventory.hasitem()) {		//drop
						movementInventory.send(ownerEntity);
						
						GUIManager.getInstance().getFocusedWindow().removeDisplayElement(movementIcon);
												
						movementIcon.setItemEntity(null);
						movementIcon.setVisible(false);
					}
					else {									//drag
						movementInventory.store(ownerEntity, itemEntity);

						GUIManager.getInstance().getFocusedWindow().addDisplayElement(movementIcon);
						
						movementIcon.setVisible(true);
					}
				}
			}
			else {
				//TODO reset dragged item if not dropped onto a UIItemCell..
			}
		}
		
		@Override
		public void down(UIDisplayElement element, int button, boolean intersect) {
			
		}
	};

	/**
	 * Create a single item cell which is capable of holding an item.
	 * @param ownerEntity The owner of this item.
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
	        movementIcon.setItemEntity(null);
        }
        
        addMouseMoveListener(mouseMoveListener);
        addMouseButtonListener(mouseButtonListener);
        
        addDisplayElement(background);
        addDisplayElement(icon);
        addDisplayElement(selectionRectangle);
        addDisplayElement(itemLabel);
    }
    
    /**
     * Set the item which this item cell contains.
     * @param itemEntity The item.
     * @param slot The slot number in the inventory of the owner.
     */
    public void setItem(EntityRef itemEntity, int slot) {
    	this.itemEntity = itemEntity;
    	icon.setItemEntity(itemEntity);
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
	 * @param enableDrag True to enable drag/drop.
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
	 * @param enableDrag True to enable the selection rectangle as the mouse is over.
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

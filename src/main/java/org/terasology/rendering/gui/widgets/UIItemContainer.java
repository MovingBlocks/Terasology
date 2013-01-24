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

import org.terasology.components.InventoryComponent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.entitySystem.EventPriority;
import org.terasology.entitySystem.EventSystem;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.entitySystem.event.ChangedComponentEvent;
import org.terasology.game.CoreRegistry;
import org.terasology.rendering.gui.framework.UIDisplayContainer;

import javax.vecmath.Vector2f;
import java.util.ArrayList;
import java.util.List;

/**
 * A container which is capable of holding multiple item cells.
 *
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 */
public class UIItemContainer extends UIDisplayContainer {

    private EntityRef entity = EntityRef.NULL;
    private List<UIItemCell> cells = new ArrayList<UIItemCell>();

    private EntityRef connectedEntity = EntityRef.NULL;

    private Vector2f cellMargin = new Vector2f(2, 2);
    private Vector2f cellSize = new Vector2f(48, 48);
    private Vector2f iconPosition = new Vector2f(2f, 2f);
    
    private int cols;

    private int slotStart = -1;
    private int slotEnd = -1;

    public UIItemContainer(int cols) {
        this.cols = cols;
    }

    @Override
    public void update() {
        super.update();
        updateInventoryCells(entity.getComponent(InventoryComponent.class));
    }

    private void fillInventoryCells(InventoryComponent entityInventory) {
        if (entityInventory != null) {
            //remove old cells
            for (UIItemCell cell : cells) {
                removeDisplayElement(cell);
            }
            cells.clear();

            //add new cells
            setVisible(true);

            int start = 0;
            int end = entityInventory.itemSlots.size();

            if (slotStart != -1) {
                start = slotStart;
            }

            if (slotEnd != -1) {
                end = slotEnd;
            }
            
            for (int i = start; i < end; ++i)
            {
                UIItemCell cell = new UIItemCell(entity, cellSize, iconPosition);
                cell.setItemEntity(entityInventory.itemSlots.get(i), i);
                cell.setSize(cellSize);
                cell.setConnected(connectedEntity);
                cell.setPosition(new Vector2f(((i - start) % cols) * (cellSize.x + cellMargin.x), ((i - start) / cols) * (cellSize.y + cellMargin.y)));
                cell.setVisible(true);

                cells.add(cell);
                addDisplayElement(cell);
            }

            setSize(new Vector2f(cols * (cellSize.x + cellMargin.x), (cells.size() / cols) * (cellSize.y + cellMargin.y)));
        }
    }

    public void updateInventoryCells(InventoryComponent entityInventory) {
        if (entityInventory != null) {
            int start = Math.max(slotStart, 0);
            for (int i = 0; i < cells.size(); ++i) {
                cells.get(i).setItemEntity(entityInventory.itemSlots.get(start), start);
                start++;
            }
        }
    }

    /**
     * Get the entity which is connected to this container for supporting fast transfer.
     *
     * @return
     */
    public EntityRef getConnected() {
        return connectedEntity;
    }

    /**
     * Set the connected entity which is connected to this container.
     * Therefore fast transfer will be enabled to this entities inventory.
     * Fast transfer means pressing an button and clicking on an item will automatically transfer the item to this entities inventory.
     *
     * @param entity The entity to connect to this container.
     */
    public void setConnected(EntityRef entity) {
        this.connectedEntity = entity;

        for (UIItemCell cell : cells) {
            cell.setConnected(connectedEntity);
        }
    }

    public List<UIItemCell> getCells() {
        return cells;
    }

    public EntityRef getEntity() {
        return entity;
    }

    public void setEntity(EntityRef entity) {
        this.entity = entity;
        InventoryComponent entityInventory = entity.getComponent(InventoryComponent.class);

        this.slotStart = -1;
        this.slotEnd = -1;

        fillInventoryCells(entityInventory);
    }

    public void setEntity(EntityRef entity, int slotStart) {
        this.entity = entity;
        InventoryComponent entityInventory = entity.getComponent(InventoryComponent.class);

        this.slotStart = slotStart;
        this.slotEnd = -1;

        fillInventoryCells(entityInventory);
    }

    public void setEntity(EntityRef entity, int slotStart, int slotEnd) {
        this.entity = entity;
        InventoryComponent entityInventory = entity.getComponent(InventoryComponent.class);

        this.slotStart = slotStart;
        this.slotEnd = slotEnd + 1;

        fillInventoryCells(entityInventory);
    }

    public Vector2f getCellMargin() {
        return cellMargin;
    }

    public void setCellMargin(Vector2f cellMargin) {
        this.cellMargin = cellMargin;
        fillInventoryCells(entity.getComponent(InventoryComponent.class));
    }

    public Vector2f getCellSize() {
        return cellSize;
    }

    public void setCellSize(Vector2f cellSize) {
        this.cellSize = cellSize;
        fillInventoryCells(entity.getComponent(InventoryComponent.class));
    }

    public int getCols() {
        return cols;
    }

    public void setCols(int cols) {
        this.cols = cols;
        fillInventoryCells(entity.getComponent(InventoryComponent.class));
    }
    
    public int getSlotStart(){
        return slotStart;
    }
    
    public int getSlotEnd(){
        return slotEnd - 1;
    }
    
    public void setIconPosition(Vector2f position){
        iconPosition = position;
    }
}


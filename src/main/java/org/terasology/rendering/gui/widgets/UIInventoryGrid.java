/*
 * Copyright 2013 Moving Blocks
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.CoreRegistry;
import org.terasology.entitySystem.EntityRef;
import org.terasology.logic.inventory.SlotBasedInventoryManager;
import org.terasology.rendering.gui.framework.UIDisplayContainer;

import javax.vecmath.Vector2f;
import java.util.ArrayList;
import java.util.List;

/**
 * A grid of inventory cells
 *
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 */
public class UIInventoryGrid extends UIDisplayContainer {
    private static final Logger logger = LoggerFactory.getLogger(UIInventoryGrid.class);

    private SlotBasedInventoryManager inventoryManager = CoreRegistry.get(SlotBasedInventoryManager.class);
    private EntityRef entity = EntityRef.NULL;
    private List<UIInventoryCell> cells = new ArrayList<UIInventoryCell>();

    private Vector2f cellMargin = new Vector2f(0, 0);
    private Vector2f cellSize = new Vector2f(48, 48);

    private int numColumns;

    private int startSlot = 0;
    private int maxSlotsInGrid = 0;

    public UIInventoryGrid(int numColumns) {
        if (inventoryManager == null) {
            logger.error("No inventory manager");
        }
        this.numColumns = numColumns;
    }

    public void linkToEntity(EntityRef entity) {
        int numSlots = inventoryManager.getNumSlots(entity);
        this.linkToEntity(entity, 0, numSlots);
    }

    public void linkToEntity(EntityRef entity, int startSlot) {
        int numSlots = inventoryManager.getNumSlots(entity);
        this.linkToEntity(entity, startSlot, Math.max(0, numSlots - startSlot));
    }

    public void linkToEntity(EntityRef entity, int startSlot, int numSlots) {
        if (!entity.equals(this.entity)) {
            this.entity = entity;
            this.startSlot = startSlot;
            this.maxSlotsInGrid = numSlots;

            fillInventoryCells();
        }
    }

    @Override
    public void update() {
        super.update();
    }

    private void fillInventoryCells() {
        //remove old cells
        for (UIInventoryCell cell : cells) {
            removeDisplayElement(cell);
        }
        cells.clear();

        int start = startSlot;
        int numSlots = inventoryManager.getNumSlots(entity);
        int numCells = Math.min(maxSlotsInGrid, numSlots);
        for (int i = 0; i < numCells; ++i) {
            int slot = (i + start) % numSlots;
            UIInventoryCell cell = new UIInventoryCell(entity, slot, cellSize);
            cell.setPosition(new Vector2f((i % numColumns) * (cellSize.x + cellMargin.x), (i / numColumns) * (cellSize.y + cellMargin.y)));

            cells.add(cell);
            addDisplayElement(cell);
        }

        setSize(new Vector2f(numColumns * (cellSize.x + cellMargin.x), (cells.size() / numColumns) * (cellSize.y + cellMargin.y)));
    }

    public List<UIInventoryCell> getCells() {
        return cells;
    }


    public Vector2f getCellMargin() {
        return new Vector2f(cellMargin);
    }

    public void setCellMargin(Vector2f cellMargin) {
        this.cellMargin.set(cellMargin);
        fillInventoryCells();
    }

    public int getStartSlot() {
        return startSlot;
    }


    public void setSelected(int selectedTool) {
        int cell = selectedTool - startSlot;
        while (cell < 0) {
            cell += inventoryManager.getNumSlots(entity);
        }
        for (int i = 0; i < cells.size(); ++i) {
            cells.get(i).setSelected(i == cell);
        }
    }
}


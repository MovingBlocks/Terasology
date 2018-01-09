/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.rendering.nui.layers.ingame.inventory;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.input.MouseInput;
import org.terasology.logic.inventory.InventoryUtils;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2i;
import org.terasology.rendering.nui.BaseInteractionListener;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.CoreWidget;
import org.terasology.rendering.nui.InteractionListener;
import org.terasology.rendering.nui.LayoutConfig;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.DefaultBinding;
import org.terasology.rendering.nui.databinding.ReadOnlyBinding;
import org.terasology.rendering.nui.events.NUIMouseClickEvent;

import java.util.Iterator;
import java.util.List;

/**
 * A grid of {@link InventoryCell} used to display an inventory of an entity.
 */
public class InventoryGrid extends CoreWidget {
    /*
    Defines maximum amount of cells displayed in a single row of InventoryGrid.
    E.g if an inventory has 20 slots and maxHorizontalCells is set to 5, the grid will have 4 rows of cells (grid size will be 5x4).
     */
    @LayoutConfig
    private int maxHorizontalCells = 10;

    /*
    Defines first inventory slot index to which an InventoryGrid should refer to.
    E.g if an inventory has 20 slots and cellOffset is set to 5, 15 cells will be drawn starting from slot no. 5 (starting from zero).
    Other slots will be not accessible by this InventoryGrid.
     */
    @LayoutConfig
    private Binding<Integer> cellOffset = new DefaultBinding<>(0);

    /*
    Defines the maximum amount of cells in an InventoryGrid
    Used together with cellOffset allows developers to provide access to parts of an entity's inventory.
    E.g if an inventory has 20 slots, maxCellCount is set to 10 and cellOffset to 5, there will be 10 cells drawn, starting from slot no. 5 and ending at cell no. 14 (starting from zero).
     */
    @LayoutConfig
    private Binding<Integer> maxCellCount = new DefaultBinding<>(Integer.MAX_VALUE);

    private List<InventoryCell> cells = Lists.newArrayList();

    //EntityRef to an entity whose inventory will be accessed using this InventoryGrid.
    private Binding<EntityRef> targetEntity = new DefaultBinding<>(EntityRef.NULL);

    private InteractionListener interactionListener = new BaseInteractionListener() {
        @Override
        public boolean onMouseClick(NUIMouseClickEvent event) {
            MouseInput mouseButton = event.getMouseButton();
            if (mouseButton == MouseInput.MOUSE_LEFT) {
                return true;
            }
            return false;
        }
    };

    @Override
    public void update(float delta) {
        super.update(delta);

        int numSlots = getNumSlots();

        // allow the UI to grow or shrink the cell count if the inventory changes size
        if (numSlots < cells.size()) {
            for (int i = cells.size(); i > numSlots && i > 0; --i) {
                cells.remove(i - 1);
            }
        } else if (numSlots > cells.size()) {
            for (int i = cells.size(); i < numSlots && i < getMaxCellCount(); ++i) {
                InventoryCell cell = new InventoryCell();
                cell.bindTargetInventory(new ReadOnlyBinding<EntityRef>() {
                    @Override
                    public EntityRef get() {
                        return getTargetEntity();
                    }
                });
                cell.bindTargetSlot(new SlotBinding(i));
                cells.add(cell);
            }
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        int numSlots = getNumSlots();
        if (numSlots == 0 || cells.isEmpty()) {
            return;
        }
        Vector2i cellSize = canvas.calculatePreferredSize(cells.get(0));
        if (cellSize.getX() == 0 || cellSize.getY() == 0) {
            return;
        }
        canvas.addInteractionRegion(interactionListener);

        int horizontalCells = Math.max(1, Math.min(maxHorizontalCells, canvas.size().getX() / cellSize.getX()));
        for (int i = 0; i < numSlots && i < cells.size(); ++i) {
            int horizPos = i % horizontalCells;
            int vertPos = i / horizontalCells;
            canvas.drawWidget(cells.get(i), Rect2i.createFromMinAndSize(horizPos * cellSize.x, vertPos * cellSize.y, cellSize.x, cellSize.y));
        }
    }

    /**
     * Returns preferred content size of the InventoryGrid.
     * @param canvas Canvas on which an InventoryGrid is drawn.
     * @param sizeHint A {@link Vector2i} representing how much available space is for this widget.
     * @return A {@link Vector2i} which represents the preferred size of this widget.
     */
    @Override
    public Vector2i getPreferredContentSize(Canvas canvas, Vector2i sizeHint) {
        int numSlots = getNumSlots();
        if (numSlots == 0 || cells.isEmpty()) {
            return Vector2i.zero();
        }
        Vector2i cellSize = canvas.calculatePreferredSize(cells.get(0));
        if (cellSize.getX() == 0 || cellSize.getY() == 0) {
            return Vector2i.zero();
        }
        int horizontalCells = Math.min(Math.min(maxHorizontalCells, numSlots), sizeHint.getX() / cellSize.getX());
        int verticalCells = ((numSlots - 1) / horizontalCells) + 1;
        return new Vector2i(horizontalCells * cellSize.x, verticalCells * cellSize.y);
    }

    /**
     * Returns an iterator over the {@link InventoryCell}s this grid displays.
     * @return Iterator over this grid's InventoryCells.
     */
    @Override
    public Iterator<UIWidget> iterator() {
        return Iterators.transform(cells.iterator(), new Function<UIWidget, UIWidget>() {
            @Override
            public UIWidget apply(UIWidget input) {
                return input;
            }
        });
    }

    /**
     * Gets maximum horizontal cells amount.
     * @return maximum horizontal cells amount.
     */
    public int getMaxHorizontalCells() {
        return maxHorizontalCells;
    }

    /**
     * Sets maximum horizontal cells amount.
     * @param maxHorizontalCells maximum horizontal cells amount.
     */
    public void setMaxHorizontalCells(int maxHorizontalCells) {
        this.maxHorizontalCells = maxHorizontalCells;
    }

    /**
     * Binds the entity to this grid whose inventory will be accessed using this widget.
     * @param binding Binding of the EntityRef type referring to the entity whose inventory will be displayed.
     */
    public void bindTargetEntity(Binding<EntityRef> binding) {
        targetEntity = binding;
    }

    /**
     * Returns an EntityRef to the entity whose inventory is displayed using this grid.
     * @return EntityRef to the entity whose inventory is displayed using this grid.
     */
    public EntityRef getTargetEntity() {
        return targetEntity.get();
    }

    /**
     *
     * @deprecated Use bindTargetEntity to assign a read only binding that is a getter
     */
    @Deprecated
    public void setTargetEntity(EntityRef val) {
        targetEntity.set(val);
    }

    /**
     * Binds the offset between cells (in px) of this widget.
     * @param binding Binding of type Integer.
     */
    public void bindCellOffset(Binding<Integer> binding) {
        cellOffset = binding;
    }

    /**
     * Gets the offset (in px) between the cells.
     * @return the offset (in px) between the cells.
     */
    public int getCellOffset() {
        return cellOffset.get();
    }

    /**
     * Sets the offset (in px) between the cells.
     * @param val the desired offset (in px) between the cells.
     */
    public void setCellOffset(int val) {
        cellOffset.set(val);
    }

    /**
     * Binds the maximum cell count.
     * @param binding Binding of type Integer.
     */
    public void bindMaxCellCount(Binding<Integer> binding) {
        maxCellCount = binding;
    }

    /**
     * Gets the maximum amount of cells that can be displayed.
     * @return maximum amount of cells that can be displayed.
     */
    public int getMaxCellCount() {
        return maxCellCount.get();
    }

    /**
     * Sets the maximum amount of cells that can be displayed.
     * @param val maximum amount of cells that can be displayed.
     */
    public void setMaxCellCount(int val) {
        maxCellCount.set(val);
    }

    /**
     * Gets the amount of cells this widget displays.
     * @return the amount of cells this widget displays.
     */
    public int getNumSlots() {
        return Math.min(InventoryUtils.getSlotCount(getTargetEntity()) - getCellOffset(), getMaxCellCount());
    }

    /**
     * Provides a getter for slots of an inventory bound to this widget corresponding to this widget.
     */
    private final class SlotBinding extends ReadOnlyBinding<Integer> {

        private int slot;

        private SlotBinding(int slot) {
            this.slot = slot;
        }

        @Override
        public Integer get() {
            return getCellOffset() + slot;
        }
    }

}

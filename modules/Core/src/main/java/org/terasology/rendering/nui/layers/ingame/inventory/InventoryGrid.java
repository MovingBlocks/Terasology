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
import org.terasology.logic.inventory.InventoryUtils;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2i;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.CoreWidget;
import org.terasology.rendering.nui.LayoutConfig;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.DefaultBinding;
import org.terasology.rendering.nui.databinding.ReadOnlyBinding;

import java.util.Iterator;
import java.util.List;

/**
 */
public class InventoryGrid extends CoreWidget {

    @LayoutConfig
    private int maxHorizontalCells = 10;
    @LayoutConfig
    private Binding<Integer> cellOffset = new DefaultBinding<>(0);
    @LayoutConfig
    private Binding<Integer> maxCellCount = new DefaultBinding<>(Integer.MAX_VALUE);

    private List<InventoryCell> cells = Lists.newArrayList();
    private Binding<EntityRef> targetEntity = new DefaultBinding<>(EntityRef.NULL);

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
        int horizontalCells = Math.min(maxHorizontalCells, canvas.size().getX() / cellSize.getX());
        for (int i = 0; i < numSlots && i < cells.size(); ++i) {
            int horizPos = i % horizontalCells;
            int vertPos = i / horizontalCells;
            canvas.drawWidget(cells.get(i), Rect2i.createFromMinAndSize(horizPos * cellSize.x, vertPos * cellSize.y, cellSize.x, cellSize.y));
        }
    }

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

    @Override
    public Iterator<UIWidget> iterator() {
        return Iterators.transform(cells.iterator(), new Function<UIWidget, UIWidget>() {
            @Override
            public UIWidget apply(UIWidget input) {
                return input;
            }
        });
    }

    public int getMaxHorizontalCells() {
        return maxHorizontalCells;
    }

    public void setMaxHorizontalCells(int maxHorizontalCells) {
        this.maxHorizontalCells = maxHorizontalCells;
    }

    public void bindTargetEntity(Binding<EntityRef> binding) {
        targetEntity = binding;
    }

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

    public void bindCellOffset(Binding<Integer> binding) {
        cellOffset = binding;
    }

    public int getCellOffset() {
        return cellOffset.get();
    }

    public void setCellOffset(int val) {
        cellOffset.set(val);
    }

    public void bindMaxCellCount(Binding<Integer> binding) {
        maxCellCount = binding;
    }

    public int getMaxCellCount() {
        return maxCellCount.get();
    }

    public void setMaxCellCount(int val) {
        maxCellCount.set(val);
    }

    public int getNumSlots() {
        return Math.min(InventoryUtils.getSlotCount(getTargetEntity()) - getCellOffset(), getMaxCellCount());
    }

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

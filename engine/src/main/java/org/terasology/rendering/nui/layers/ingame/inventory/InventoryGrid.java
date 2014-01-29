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
import org.terasology.logic.inventory.SlotBasedInventoryManager;
import org.terasology.math.Rect2i;
import org.terasology.math.Vector2i;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.CoreWidget;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.DefaultBinding;
import org.terasology.rendering.nui.databinding.ReadOnlyBinding;

import java.util.Iterator;
import java.util.List;

/**
 * @author Immortius
 */
public class InventoryGrid extends CoreWidget {

    private int maxHorizontalCells = 10;
    private List<InventoryCell> cells = Lists.newArrayList();

    private Binding<EntityRef> targetEntity = new DefaultBinding<>(EntityRef.NULL);
    private SlotBasedInventoryManager inventoryManager = CoreRegistry.get(SlotBasedInventoryManager.class);

    @Override
    public void update(float delta) {
        super.update(delta);

        int numSlots = inventoryManager.getNumSlots(getTargetEntity());
        if (numSlots > cells.size()) {
            for (int i = cells.size(); i < numSlots; ++i) {
                InventoryCell cell = new InventoryCell();
                cell.bindTargetItem(new SlotBinding(i));
                cells.add(cell);
            }
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        int numSlots = inventoryManager.getNumSlots(getTargetEntity());
        if (numSlots != 0) {
            Vector2i cellSize = canvas.calculatePreferredSize(cells.get(0));
            int horizontalCells = Math.min(maxHorizontalCells, canvas.size().getX() / cellSize.getX());
            for (int i = 0; i < numSlots && i < cells.size(); ++i) {
                int horizPos = i % horizontalCells;
                int vertPos = i / horizontalCells;
                canvas.drawWidget(cells.get(i), Rect2i.createFromMinAndSize(horizPos * cellSize.x, vertPos * cellSize.y, cellSize.x, cellSize.y));
            }
        }
    }

    @Override
    public Vector2i getPreferredContentSize(Canvas canvas, Vector2i sizeHint) {
        int numSlots = inventoryManager.getNumSlots(getTargetEntity());
        if (numSlots != 0) {
            Vector2i cellSize = canvas.calculatePreferredSize(cells.get(0));
            int horizontalCells = Math.min(maxHorizontalCells, sizeHint.getX() / cellSize.getX());
            int verticalCells = ((numSlots - 1) / horizontalCells) + 1;
            return new Vector2i(horizontalCells * cellSize.x, verticalCells * cellSize.y);
        }
        return Vector2i.zero();
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

    public void setTargetEntity(EntityRef val) {
        targetEntity.set(val);
    }

    private class SlotBinding extends ReadOnlyBinding<EntityRef> {

        private int slot;

        private SlotBinding(int slot) {
            this.slot = slot;
        }

        @Override
        public EntityRef get() {
            return inventoryManager.getItemInSlot(getTargetEntity(), slot);
        }
    }
}

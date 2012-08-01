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

import com.google.common.collect.Lists;
import org.terasology.components.InventoryComponent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.rendering.gui.framework.UIDisplayContainer;

import javax.vecmath.Vector2f;
import java.util.List;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class UIInventoryNew extends UIDisplayContainer implements UIInventoryCellNew.CellSubscriber {

    EntityRef entity = EntityRef.NULL;
    Vector2f cellBorder = new Vector2f(2, 2);
    Vector2f cellSize = new Vector2f(48, 48);
    List<UIInventoryCellNew> cells = Lists.newArrayList();
    List<InventorySubscriber> subscribers = Lists.newArrayList();
    private int targetWidth;

    public interface InventorySubscriber {
        public void itemClicked(UIInventoryNew inventoryNew, int slot);
    }

    public UIInventoryNew(int width) {
        targetWidth = width;
    }

    public EntityRef getEntity() {
        return entity;
    }

    public void setEntity(EntityRef entity) {
        this.entity = entity;
        InventoryComponent inventory = entity.getComponent(InventoryComponent.class);
        for (UIInventoryCellNew cell : cells) {
            removeDisplayElement(cell);
        }
        cells.clear();

        if (inventory == null) {
            setVisible(false);
        } else {
            setVisible(true);
            float height = Math.max(inventory.itemSlots.size() / targetWidth, 1);
            setSize(new Vector2f(targetWidth * (cellSize.x + cellBorder.x), height * (cellSize.y + cellBorder.y)));

            for (int i = 0; i < inventory.itemSlots.size(); ++i) {
                UIInventoryCellNew cell = new UIInventoryCellNew(entity, i, cellSize);
                cells.add(cell);
                cell.setSize(cellSize);
                cell.setPosition(new Vector2f(i % targetWidth * (cellSize.x + cellBorder.x), i / targetWidth * (cellSize.y + cellBorder.y)));
                cell.setVisible(true);
                cell.subscribe(this);
                addDisplayElement(cell);
            }
        }
    }

    public void onCellActivated(UIInventoryCellNew cell) {
        for (InventorySubscriber subscriber : subscribers) {
            subscriber.itemClicked(this, cell.getSlot());
        }
    }

    public void subscribe(InventorySubscriber subscriber) {
        subscribers.add(subscriber);
    }

    public void unsubscribe(InventorySubscriber subscriber) {
        subscribers.remove(subscriber);
    }
}
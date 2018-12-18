/*
 * Copyright 2018 MovingBlocks
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

package org.terasology.rendering.nui;

import org.terasology.input.Keyboard;
import org.terasology.rendering.nui.events.NUIKeyEvent;
import org.terasology.rendering.nui.layouts.ScrollableArea;

/**
 * Parent for widgets that can be tabbed to.
 */
public abstract class WidgetWithOrder extends CoreWidget {

    @LayoutConfig
    protected int order = TabbingManager.UNINITIALIZED_DEPTH;
    protected boolean initialized = false;

    private boolean added = false;

    protected ScrollableArea parent;

    public WidgetWithOrder() {
        this.setId("");
    }

    public WidgetWithOrder(String id) {
        this.setId(id);
    }

    public ScrollableArea getParent() {
        return parent;
    }

    public void setParent(ScrollableArea area) {
        parent = area;
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
    }
    @Override
    public String getMode() {
        if (isFocused()) {
            return ACTIVE_MODE;
        }
        return DEFAULT_MODE;
    }
    public int getOrder() {
        if (order == TabbingManager.UNINITIALIZED_DEPTH) {
            order = TabbingManager.getNewNextNum();
            TabbingManager.addToWidgetsList(this);
            TabbingManager.addToUsedNums(order);
            added = true;
        } else if (!added) {
            TabbingManager.addToWidgetsList(this);
            TabbingManager.addToUsedNums(order);
            added = true;
        }
        return order;
    }

    @Override
    public boolean onKeyEvent(NUIKeyEvent event) {
        if (event.isDown()) {
            int keyId = event.getKey().getId();
            if (keyId == Keyboard.KeyId.UP) {
                if (parent != null && !TabbingManager.isWidgetOpen()) {
                    parent.scroll(true);
                }
                return true;
            } else if (keyId == Keyboard.KeyId.DOWN) {
                if (parent != null && !TabbingManager.isWidgetOpen()) {
                    parent.scroll(false);
                }
                return true;
            }
        }
        return super.onKeyEvent(event);
    }
}

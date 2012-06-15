/*
 * Copyright 2011 Benjamin Glatzel <benjamin.glatzel@me.com>.
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
package org.terasology.rendering.gui.menus;

import org.terasology.events.input.binds.InventoryButton;
import org.terasology.rendering.gui.components.UIInventory;
import org.terasology.rendering.gui.framework.UIDisplayWindow;

/**
 * The player's inventory.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class UIInventoryScreen extends UIDisplayWindow {

    private final UIInventory _inventory;

    public UIInventoryScreen() {
        _inventory = new UIInventory();
        _inventory.setVisible(true);
        addDisplayElement(_inventory);

        update();
        setModal(true);
    }

    @Override
    public void update() {
        super.update();
        _inventory.center();
    }

    @Override
    public boolean processBindButton(String id, boolean pressed) {
        if (pressed && InventoryButton.ID.equals(id)) {
            close(true);
            return true;
        }
        return false;
    }
}

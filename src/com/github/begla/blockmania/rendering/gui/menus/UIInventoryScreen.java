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
package com.github.begla.blockmania.rendering.gui.menus;

import com.github.begla.blockmania.rendering.gui.components.UIInventory;
import com.github.begla.blockmania.rendering.gui.framework.UIDisplayRenderer;

/**
 * The player's inventory.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class UIInventoryScreen extends UIDisplayRenderer {

    private final UIInventory _inventory;

    public UIInventoryScreen() {
        _inventory = new UIInventory();
        _inventory.setVisible(true);
        addDisplayElement(_inventory);
    }

    @Override
    public void update() {
        super.update();
        _inventory.center();
    }
}

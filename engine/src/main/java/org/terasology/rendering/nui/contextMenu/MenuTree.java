/*
 * Copyright 2016 MovingBlocks
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
package org.terasology.rendering.nui.contextMenu;

import com.google.common.collect.Maps;

import java.util.Map;
import java.util.function.Consumer;

/**
 * A data structure to store {@link ContextMenuScreen} options and submenues.
 */
public class MenuTree {
    /**
     * The options of this menu.
     */
    private Map<String, AbstractContextMenuItem> options = Maps.newLinkedHashMap();

    /**
     * The submenues of this menu.
     */
    private Map<String, MenuTree> submenues = Maps.newLinkedHashMap();

    /**
     * Whether this menu is visible.
     */
    private boolean visible;

    public MenuTree(boolean visible) {
        this.visible = visible;
    }

    public Map<String, AbstractContextMenuItem> getOptions() {
        return this.options;
    }

    public <E> void addOption(String name, Consumer<E> consumer, E item) {
        options.put(name, new ContextMenuOption<E>(consumer, item, true));
    }

    public Map<String, MenuTree> getSubmenues() {
        return this.submenues;
    }

    public void addSubmenu(String name, MenuTree tree) {
        submenues.put(name, tree);
        options.put(name, new ContextMenuOption<>(t -> {
            // If a submenu is shown, hide all the other submenues of this menu.
            if (!t.isVisible()) {
                for (MenuTree submenu : submenues.values()) {
                    submenu.setVisible(false);
                }
            }
            t.setVisible(!t.isVisible());
        }, tree, false));
    }

    public boolean isVisible() {
        return this.visible;
    }

    private void setVisible(boolean visible) {
        // Also hide all of this menu's submenues.
        if (!visible) {
            for (MenuTree submenu : submenues.values()) {
                submenu.setVisible(false);
            }
        }
        this.visible = visible;
    }
}

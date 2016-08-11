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

import com.google.common.collect.Lists;
import org.terasology.math.geom.Vector2i;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.ReadOnlyBinding;
import org.terasology.rendering.nui.widgets.UIList;

import java.util.List;

/**
 * A builder class to initialize and display {@link ContextMenuScreen} instances.
 * <p>
 * Should be used in favor of manually creating the screen.
 */
public class ContextMenuBuilder {
    private ContextMenuBuilder() {

    }

    /**
     * Initialize and display a {@link ContextMenuScreen} based on a given menu tree.
     *
     * @param manager  The {@link NUIManager} to be used to display the screen.
     * @param position The position of the initial menu tree.
     * @param tree     The menu tree the context menu is based on.
     */
    public static void showContextMenu(NUIManager manager, Vector2i position, MenuTree tree) {
        if (!manager.isOpen(ContextMenuScreen.ASSET_URI)) {
            manager.pushScreen(ContextMenuScreen.ASSET_URI, ContextMenuScreen.class);
        }

        ContextMenuScreen contextMenuScreen = (ContextMenuScreen) manager.getScreen(ContextMenuScreen.ASSET_URI);
        contextMenuScreen.setMenuWidgets(getMenuLevelList(manager, new VisibleTree(tree, true)));
        contextMenuScreen.setPosition(position);
    }

    private static List<UIList<String>> getMenuLevelList(NUIManager manager, VisibleTree tree) {
        List<UIList<String>> menuLevels = Lists.newArrayList();

        menuLevels.add(getList(manager, tree));
        for (MenuTree submenu : tree.getSubmenues().values()) {
            menuLevels.addAll(getMenuLevelList(manager, (VisibleTree) submenu));
        }

        return menuLevels;
    }

    private static UIList<String> getList(NUIManager manager, VisibleTree tree) {
        UIList<String> list = new UIList<>();
        list.setList(Lists.newArrayList(tree.getOptions().keySet()));
        list.setCanBeFocus(false);
        list.bindSelection(new Binding<String>() {
            @Override
            public String get() {
                return null;
            }

            @Override
            public void set(String value) {
                tree.getOptions().get(value).select();
                if (tree.getOptions().get(value).isFinalized()) {
                    manager.closeScreen(ContextMenuScreen.ASSET_URI);
                }
            }
        });
        list.bindVisible(new ReadOnlyBinding<Boolean>() {
            @Override
            public Boolean get() {
                return tree.isVisible();
            }
        });

        return list;
    }

    private static class VisibleTree extends MenuTree {
        private boolean visible;

        public VisibleTree(MenuTree tree, boolean visible) {
            this.options = tree.getOptions();
            for (String name : tree.submenues.keySet()) {
                this.addSubmenu(name, new VisibleTree(tree.submenues.get(name), false));
            }
            this.visible = visible;
        }

        @Override
        public void addSubmenu(String name, MenuTree tree) {
            submenues.put(name, tree);
            options.put(name, new ContextMenuOption<>(t -> {
                // If a submenu is shown, hide all the other submenues of this menu.
                if (!t.isVisible()) {
                    for (MenuTree submenu : submenues.values()) {
                        ((VisibleTree) submenu).setVisible(false);
                    }
                }
                t.setVisible(!t.isVisible());
            }, (VisibleTree) tree, false));
        }

        private boolean isVisible() {
            return visible;
        }

        private void setVisible(boolean visible) {
            this.visible = visible;
        }
    }
}

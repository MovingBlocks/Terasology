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
import org.joml.Vector2i;
import org.terasology.module.sandbox.API;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.nui.databinding.Binding;
import org.terasology.nui.databinding.ReadOnlyBinding;
import org.terasology.nui.widgets.UIList;

import java.util.List;

/**
 * A utility class to initialize and display {@link ContextMenuScreen} instances.
 * <p>
 * Should be used in favor of manually creating the screen.
 */
@API
public final class ContextMenuUtils {
    private ContextMenuUtils() {

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

    private static List<UIList<AbstractContextMenuItem>> getMenuLevelList(NUIManager manager, VisibleTree tree) {
        List<UIList<AbstractContextMenuItem>> menuLevels = Lists.newArrayList();

        menuLevels.add(getList(manager, tree));
        for (AbstractContextMenuItem item : tree.getOptions()) {
            if (item instanceof VisibleTree) {
                menuLevels.addAll(getMenuLevelList(manager, (VisibleTree) item));
            }
        }

        return menuLevels;
    }

    private static UIList<AbstractContextMenuItem> getList(NUIManager manager, VisibleTree tree) {
        UIList<AbstractContextMenuItem> list = new UIList<>();
        list.setList(tree.getOptions());
        list.setCanBeFocus(false);
        list.bindSelection(new Binding<AbstractContextMenuItem>() {
            @Override
            public AbstractContextMenuItem get() {
                return null;
            }

            @Override
            public void set(AbstractContextMenuItem value) {
                if (tree.select(value)) {
                    manager.closeScreen(ContextMenuScreen.ASSET_URI);
                }
            }
        });
        list.bindVisible(new ReadOnlyBinding<Boolean>() {
            @Override
            public Boolean get() {
                return tree.visible;
            }
        });

        return list;
    }

    private static class VisibleTree implements AbstractContextMenuItem {
        private String name;
        private List<AbstractContextMenuItem> options = Lists.newArrayList();
        private boolean visible;

        VisibleTree(MenuTree tree, boolean visible) {
            this.name = tree.getName();
            for (AbstractContextMenuItem option : tree.getOptions()) {
                if (option instanceof MenuTree) {
                    this.options.add(new VisibleTree((MenuTree) option, false));
                } else {
                    this.options.add(option);
                }
            }
            this.visible = visible;
        }

        /**
         * @param option The option to be selected.
         * @return Whether the context menu should be closed after the option is selected
         */
        public boolean select(AbstractContextMenuItem option) {
            if (options.contains(option)) {
                if (option instanceof ContextMenuOption) {
                    ((ContextMenuOption) option).select();
                    return ((ContextMenuOption) option).isFinalized();
                } else if (option instanceof VisibleTree) {
                    ((VisibleTree) option).visible = !((VisibleTree) option).visible;
                    return false;
                } else {
                    return false;
                }
            }
            return false;
        }

        public void setVisible(boolean visible) {
            this.visible = visible;
        }

        public List<AbstractContextMenuItem> getOptions() {
            return options;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}

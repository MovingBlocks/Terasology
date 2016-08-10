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

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.terasology.math.geom.Vector2i;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.ReadOnlyBinding;
import org.terasology.rendering.nui.widgets.UIList;
import org.terasology.rendering.nui.widgets.UpdateListener;

import java.util.List;

public class ContextMenuBuilder {
    private NUIManager manager;
    /**
     *
     */
    private ContextMenuTree tree;
    /**
     * Listeners fired when an item is selected.
     */
    private List<UpdateListener> selectionListeners = Lists.newArrayList();
    /**
     * Listeners fired when the menu is closed.
     */
    private List<UpdateListener> closeListeners = Lists.newArrayList();
    /**
     * Listeners fired when the menu is closed, either with or without
     * selecting an option.
     */
    private List<UpdateListener> screenClosedListeners = Lists.newArrayList();

    public ContextMenuBuilder(NUIManager manager) {
        this.manager = manager;
    }

    public void showContextMenu(Vector2i position) {
        if (!manager.isOpen(ContextMenuScreen.ASSET_URI)) {
            manager.pushScreen(ContextMenuScreen.ASSET_URI, ContextMenuScreen.class);
        }

        ContextMenuScreen contextMenuScreen = (ContextMenuScreen) manager.getScreen(ContextMenuScreen.ASSET_URI);
        tree.setVisible(true);
        contextMenuScreen.setMenuLevels(getMenuLevelList(tree));
        contextMenuScreen.setPosition(position);
        contextMenuScreen.subscribeClose(() -> closeListeners.forEach(UpdateListener::onAction));
        contextMenuScreen.subscribeScreenClosed(() -> screenClosedListeners.forEach(UpdateListener::onAction));
    }

    public void setTree(ContextMenuTree tree) {
        this.tree = tree;
    }

    private List<UIList<String>> getMenuLevelList(ContextMenuTree tree) {
        List<UIList<String>> menuLevels = Lists.newArrayList();

        menuLevels.add(getList(tree));
        for (ContextMenuTree submenu : tree.getSubmenues().values()) {
            menuLevels.addAll(getMenuLevelList(submenu));
        }

        return menuLevels;
    }

    private UIList<String> getList(ContextMenuTree tree) {
        UIList<String> list = new UIList<>();
        list.setList(Lists.newArrayList(tree.getOptions().keySet()));
        list.bindSelection(new Binding<String>() {
            @Override
            public String get() {
                return null;
            }

            @Override
            public void set(String value) {
                tree.getOptions().get(value).accept();
                if (tree.getOptions().get(value).isFinalized()) {
                    selectionListeners.forEach(UpdateListener::onAction);
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

    public void subscribeSelection(UpdateListener listener) {
        Preconditions.checkNotNull(listener);
        selectionListeners.add(listener);
    }

    public void unsubscribeSelection(UpdateListener listener) {
        Preconditions.checkNotNull(listener);
        selectionListeners.remove(listener);
    }

    public void subscribeClose(UpdateListener listener) {
        Preconditions.checkNotNull(listener);
        closeListeners.add(listener);
    }

    public void unsubscribeClose(UpdateListener listener) {
        Preconditions.checkNotNull(listener);
        closeListeners.remove(listener);
    }

    public void subscribeScreenClosed(UpdateListener listener) {
        Preconditions.checkNotNull(listener);
        screenClosedListeners.add(listener);
    }

    public void unsubscribeScreenclosed(UpdateListener listener) {
        Preconditions.checkNotNull(listener);
        screenClosedListeners.remove(listener);
    }
}

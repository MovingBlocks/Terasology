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

import com.google.api.client.util.Maps;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.terasology.math.geom.Vector2i;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.widgets.UpdateListener;

import java.util.List;
import java.util.Map;

/**
 * A builder class to construct and show {@link ContextMenuScreen} instances.
 * <p>
 * Should be used in favor of manually creating the screen.
 */
public class ContextMenuBuilder {
    private Map<String, ContextMenuLevel> menuLevels = Maps.newLinkedHashMap();
    /**
     * Listeners fired when an item is selected.
     */
    private List<UpdateListener> selectionListeners = Lists.newArrayList();
    /**
     * Listeners fired when the menu is closed.
     */
    private List<UpdateListener> closeListeners = Lists.newArrayList();
    /**
     *
     */
    private List<UpdateListener> screenClosedListeners = Lists.newArrayList();

    public ContextMenuLevel getLevel(String key) {
        return menuLevels.get(key);
    }

    public ContextMenuLevel addLevel(String key) {
        ContextMenuLevel level = new ContextMenuLevel();
        menuLevels.put(key, level);
        return level;
    }

    /**
     * Initializes and pushes the {@link ContextMenuScreen} with the existing list of options.
     *
     * @param manager  The {@link NUIManager} the screen is to be pushed to.
     * @param position The position of the context menu within the screen.
     */
    public void show(NUIManager manager, Vector2i position) {
        if (!manager.isOpen(ContextMenuScreen.ASSET_URI)) {
            manager.pushScreen(ContextMenuScreen.ASSET_URI, ContextMenuScreen.class);
        }

        ContextMenuLevel firstLevel = menuLevels.entrySet().iterator().next().getValue();
        firstLevel.setVisible(true);
        firstLevel.setPosition(position);
        for (ContextMenuLevel level : menuLevels.values()) {
            level.getMenuWidget().bindSelection(new Binding<String>() {
                @Override
                public String get() {
                    return null;
                }

                @Override
                public void set(String value) {
                    level.accept(value);
                    if (((ContextMenuOption) level.getOptions().get(value)).isFinalized()) {
                        selectionListeners.forEach(UpdateListener::onAction);
                        manager.closeScreen(ContextMenuScreen.ASSET_URI);
                    }
                }
            });
        }

        ContextMenuScreen contextMenuScreen = (ContextMenuScreen) manager.getScreen(ContextMenuScreen.ASSET_URI);
        contextMenuScreen.setMenuLevels(Lists.newArrayList(menuLevels.values()));

        contextMenuScreen.subscribeClose(() -> {
            closeListeners.forEach(UpdateListener::onAction);
        });

        contextMenuScreen.subscribeScreenClosed(() -> {
            screenClosedListeners.forEach(UpdateListener::onAction);
        });
    }

    /**
     * Subscribe to an item being selected.
     *
     * @param listener The listener to be added.
     */
    public void subscribeSelection(UpdateListener listener) {
        Preconditions.checkNotNull(listener);
        selectionListeners.add(listener);
    }

    /**
     * Unsubscribe from an item being selected.
     *
     * @param listener The listener to be removed.
     */
    public void unsubscribeSelection(UpdateListener listener) {
        Preconditions.checkNotNull(listener);
        selectionListeners.remove(listener);
    }

    /**
     * Subscribe to the menu being closed.
     *
     * @param listener The listener to be added.
     */
    public void subscribeClose(UpdateListener listener) {
        Preconditions.checkNotNull(listener);
        closeListeners.add(listener);
    }

    /**
     * Unsubscribe from an item being selected.
     *
     * @param listener The listener to be removed.
     */
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

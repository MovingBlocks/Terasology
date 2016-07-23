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

import com.google.api.client.util.Lists;
import com.google.api.client.util.Maps;
import org.terasology.math.geom.Vector2i;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.widgets.UIList;

import java.util.Map;
import java.util.function.Consumer;

/**
 * A single level in the context menu.
 */
public class ContextMenuLevel {
    /**
     * A map of context menu options to their respective names.
     */
    private Map<String, ContextMenuOption> options = Maps.newHashMap();
    /**
     * The widget used to draw the menu.
     */
    private UIList<String> menuWidget = new UIList<>();
    /**
     * The position of the menu widget.
     */
    private Vector2i position = Vector2i.zero();
    /**
     * Whether the context menu is visible.
     */
    private boolean visible;

    public ContextMenuLevel() {
        menuWidget.setCanBeFocus(false);
        menuWidget.bindSelection(new Binding<String>() {
            @Override
            public String get() {
                return null;
            }

            @Override
            public void set(String value) {
                accept(value);
            }
        });
    }

    /**
     * @return The position of the menu widget.
     */
    public Vector2i getPosition() {
        return position;
    }

    /**
     * @param position The new position of the menu widget.
     */
    public void setPosition(Vector2i position) {
        this.position = position;
    }

    /**
     * @return The menu widget.
     */
    public UIList<String> getMenuWidget() {
        return menuWidget;
    }

    /**
     * @return The context menu option map.
     */
    public Map getOptions() {
        return options;
    }

    /**
     * Add a new option to the map of options.
     * @param name The name for the option.
     * @param consumer A consumer operation.
     * @param item The object to be passed to the consumer operation.
     * @param finalized Whether the option is final (i.e. on selection closes the context menu
     * and triggers the relevant listeners, if any).
     * @param <E> The type of the object passed to the option.
     */
    public <E> void addOption(String name, Consumer<E> consumer, E item, boolean finalized) {
        options.put(name, new ContextMenuOption<E>(consumer, item, finalized));

        // Update the widget's list.
        menuWidget.setList(Lists.newArrayList(options.keySet()));
    }

    /**
     * @param value The name of the option to be accepted.
     */
    public void accept(String value) {
        options.get(value).accept();
    }

    /**
     * @return Whether the level is currently visible.
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * @param visible The visibility state.
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}

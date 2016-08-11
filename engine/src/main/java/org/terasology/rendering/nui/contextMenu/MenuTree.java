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
    protected Map<String, AbstractContextMenuItem> options = Maps.newLinkedHashMap();

    /**
     * The submenues of this menu.
     */
    protected Map<String, MenuTree> submenues = Maps.newLinkedHashMap();

    Map<String, AbstractContextMenuItem> getOptions() {
        return this.options;
    }

    public <E> void addOption(String name, Consumer<E> consumer, E item) {
        options.put(name, new ContextMenuOption<E>(consumer, item, true));
    }

    Map<String, MenuTree> getSubmenues() {
        return this.submenues;
    }

    public void addSubmenu(String name, MenuTree tree) {
        submenues.put(name, tree);
    }
}

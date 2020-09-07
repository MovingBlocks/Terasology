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
import org.terasology.gestalt.module.sandbox.API;

import java.util.List;
import java.util.function.Consumer;

/**
 * A data structure to store {@link ContextMenuScreen} options and submenues.
 */
@API
public final class MenuTree implements AbstractContextMenuItem {
    /**
     * The options of this menu.
     */
    protected List<AbstractContextMenuItem> options = Lists.newArrayList();

    /**
     * The name of the menu.
     */
    private String name;

    public MenuTree(String name) {
        this.name = name;
    }

    List<AbstractContextMenuItem> getOptions() {
        return this.options;
    }

    public <E> void addOption(String optionName, Consumer<E> consumer, E item) {
        options.add(new ContextMenuOption<E>(optionName, consumer, item, true));
    }

    public void addSubmenu(MenuTree tree) {
        options.add(tree);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}

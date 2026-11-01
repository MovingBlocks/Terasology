// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.contextMenu;

import com.google.common.collect.Lists;
import org.terasology.context.annotation.API;

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

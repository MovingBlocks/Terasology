// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.logic.common;

import org.terasology.engine.network.Replicate;
import org.terasology.engine.world.block.items.AddToBlockBasedItem;
import org.terasology.gestalt.entitysystem.component.Component;

@AddToBlockBasedItem
public class DisplayNameComponent implements Component<DisplayNameComponent> {
    @Replicate
    public String name = "";
    @Replicate
    public String description = "";


    @Override
    public String toString() {
        return String.format("DisplayName(name = '%s', description = '%s')", name, description);
    }

    @Override
    public void copy(DisplayNameComponent other) {
        this.name = other.name;
        this.description = other.description;
    }
}

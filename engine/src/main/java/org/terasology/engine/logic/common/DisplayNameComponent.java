// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.logic.common;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.network.Replicate;
import org.terasology.engine.world.block.items.AddToBlockBasedItem;

/**
 */
@AddToBlockBasedItem
public class DisplayNameComponent implements Component {
    @Replicate
    public String name = "";
    @Replicate
    public String description = "";


    @Override
    public String toString() {
        return String.format("DisplayName(name = '%s', description = '%s')", name, description);
    }
}

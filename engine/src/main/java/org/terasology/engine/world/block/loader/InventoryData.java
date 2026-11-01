// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.block.loader;

import org.terasology.context.annotation.API;

@API
public class InventoryData {
    private boolean directPickup;
    private boolean stackable = true;

    public InventoryData() {

    }

    public InventoryData(InventoryData other) {
        this.directPickup = other.directPickup;
        this.stackable = other.stackable;
    }

    public boolean isDirectPickup() {
        return directPickup;
    }

    public void setDirectPickup(boolean directPickup) {
        this.directPickup = directPickup;
    }

    public boolean isStackable() {
        return stackable;
    }

    public void setStackable(boolean stackable) {
        this.stackable = stackable;
    }
}

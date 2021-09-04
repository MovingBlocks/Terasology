// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.logic.inventory;

import org.terasology.gestalt.entitysystem.component.Component;


public class PickupComponent implements Component<PickupComponent> {
    public long timeToPickUp;

    public long timeDropped;

    @Override
    public void copyFrom(PickupComponent other) {
        this.timeToPickUp = other.timeToPickUp;
        this.timeDropped = other.timeDropped;
    }
}

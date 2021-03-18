// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.logic.inventory;

import org.terasology.engine.entitySystem.Component;


public class PickupComponent implements Component {
    public long timeToPickUp;

    public long timeDropped;
}

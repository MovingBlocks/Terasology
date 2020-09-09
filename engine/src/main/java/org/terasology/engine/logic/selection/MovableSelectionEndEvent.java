// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.selection;

import org.terasology.engine.entitySystem.event.Event;
import org.terasology.engine.math.Region3i;
import org.terasology.gestalt.module.sandbox.API;

/**
 * This event is sent when the player finalizes the position of a moving selection by clicking the left mouse button.
 * This event marks the end of the binding of the camera position with the selected region and provides the final
 * selected region.
 */
@API
public class MovableSelectionEndEvent implements Event {
    /**
     * The final position of the selected region
     */
    private final Region3i finalRegion;

    public MovableSelectionEndEvent(Region3i selectedRegion) {
        this.finalRegion = selectedRegion;
    }

    public Region3i getFinalRegion() {
        return this.finalRegion;
    }
}

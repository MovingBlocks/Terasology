// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.selection;

import org.terasology.engine.world.block.BlockRegion;
import org.terasology.gestalt.entitysystem.event.Event;
import org.terasology.context.annotation.API;

/**
 * This event is sent when the player finalizes the position of a moving selection by clicking the left mouse button.
 * This event marks the end of the binding of the camera position with the selected region and provides the final selected
 * region.
 */
@API
public class MovableSelectionEndEvent implements Event {
    /**
     * The final position of the selected region
     */
    private BlockRegion finalRegion;

    public MovableSelectionEndEvent(BlockRegion selectedRegion) {
        this.finalRegion = selectedRegion;
    }

    public BlockRegion getFinalRegion() {
        return this.finalRegion;
    }
}

/*
 * Copyright 2019 MovingBlocks
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
package org.terasology.logic.selection;

import org.terasology.module.sandbox.API;
import org.terasology.entitySystem.event.Event;
import org.terasology.math.Region3i;
import org.terasology.world.block.BlockRegion;

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

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
package org.terasology.world.block.entity.placement;

import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.List;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.AbstractConsumableEvent;
import org.terasology.math.Region3i;
import org.terasology.math.geom.Vector3i;

/**
 * An event to verify the placement of blocks within the given region(s).
 * <p>
 * The event is send when a block placement should be performed. Any system can veto the placement by consuming this event.
 */
public class CheckPlacementPermissionEvent extends AbstractConsumableEvent {
    private EntityRef instigator;
    private List<Region3i> placementRegions;

    public CheckPlacementPermissionEvent(Region3i placementRegion) {
        this(Collections.singletonList(placementRegion), EntityRef.NULL);
    }

    public CheckPlacementPermissionEvent(Region3i placementRegion, EntityRef instigator) {
        this(Collections.singletonList(placementRegion), instigator);
    }

    public CheckPlacementPermissionEvent(List<Region3i> placementRegions, EntityRef instigator) {
        this.instigator = instigator;
        this.placementRegions = placementRegions;
    }

    public CheckPlacementPermissionEvent(List<Region3i> placementRegions) {
        this(placementRegions, EntityRef.NULL);
    }

    public CheckPlacementPermissionEvent(Vector3i pos) {
        this(Region3i.createFromCenterExtents(pos, 1), EntityRef.NULL);
    }

    public CheckPlacementPermissionEvent(Vector3i pos, EntityRef instigator) {
        this(Region3i.createFromCenterExtents(pos, 1), instigator);
    }

    /**
     * The instigator of the block placement request.
     *
     * @return the instigating entity of the block placement
     */
    public EntityRef getInstigator() {
        return instigator;
    }

    /**
     * The regions affected by the block placement.
     * <p>
     * The block placements will happen inside the given regions. Any block encompassed by the regions might be affected by the block placement.
     *
     * @return a list of affected regions
     */
    public List<Region3i> getPlacementRegions() {
        return ImmutableList.copyOf(placementRegions);
    }
}

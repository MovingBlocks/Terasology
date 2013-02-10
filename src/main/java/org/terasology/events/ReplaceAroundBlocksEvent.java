package org.terasology.events;

import org.terasology.entitySystem.AbstractEvent;
import org.terasology.math.Vector3i;

public class ReplaceAroundBlocksEvent extends AbstractEvent {
    private Vector3i placementPos;

    public ReplaceAroundBlocksEvent(Vector3i placementPos){
        this.placementPos = placementPos;
    }

    public Vector3i getPlasePosition(){
        return placementPos;
    }
}

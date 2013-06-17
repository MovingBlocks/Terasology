package org.terasology.world.block.items;

import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.event.Event;
import org.terasology.math.Vector3i;

/**
 * @author Immortius
 */
public class OnBlockItemPlaced implements Event {
    private Vector3i position;
    private EntityRef placedBlock;

    public OnBlockItemPlaced(Vector3i pos, EntityRef placedBlock) {
        this.placedBlock = placedBlock;
    }

    public Vector3i getPosition() {
        return position;
    }

    public EntityRef getPlacedBlock() {
        return placedBlock;
    }
}

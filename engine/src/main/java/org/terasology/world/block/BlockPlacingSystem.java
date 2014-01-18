package org.terasology.world.block;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.EventPriority;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.entitySystem.systems.In;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.math.Vector3i;
import org.terasology.world.WorldComponent;
import org.terasology.world.WorldProvider;

import java.util.Map;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class BlockPlacingSystem implements ComponentSystem {
    @In
    private WorldProvider worldProvider;

    @Override
    public void initialise() {
    }

    @Override
    public void shutdown() {
    }

    @ReceiveEvent(components = {WorldComponent.class}, priority = EventPriority.PRIORITY_TRIVIAL)
    public void placeBlockInWorld(PlaceBlocks event, EntityRef world) {
        for (Map.Entry<Vector3i, Block> blockEntry : event.getBlocks().entrySet()) {
            worldProvider.setBlock(blockEntry.getKey(), blockEntry.getValue());
        }
    }
}

package org.terasology.logic.world;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.terasology.componentSystem.UpdateSubscriberSystem;
import org.terasology.components.HealthComponent;
import org.terasology.components.world.BlockComponent;
import org.terasology.entitySystem.*;
import org.terasology.entitySystem.event.AddComponentEvent;
import org.terasology.entitySystem.event.RemovedComponentEvent;
import org.terasology.game.CoreRegistry;
import org.terasology.math.Vector3i;
import org.terasology.model.blocks.Block;

import java.util.List;
import java.util.Map;

/**
 * Manages creation and lookup of entities linked to blocks
 * @author Immortius <immortius@gmail.com>
 */
public interface BlockEntityRegistry {

    EntityRef getEntityAt(Vector3i blockPosition);
    EntityRef getOrCreateEntityAt(Vector3i blockPosition);
}

package org.terasology.logic.world;

import org.terasology.entitySystem.EntityRef;
import org.terasology.math.Vector3i;

import javax.vecmath.Tuple3i;

/**
 * Manages creation and lookup of entities linked to blocks
 *
 * @author Immortius <immortius@gmail.com>
 */
public interface BlockEntityRegistry {

    EntityRef getEntityAt(Vector3i blockPosition);

    EntityRef getOrCreateEntityAt(Vector3i blockPosition);
}

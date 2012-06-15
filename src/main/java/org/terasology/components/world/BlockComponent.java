package org.terasology.components.world;

import org.terasology.entitySystem.Component;
import org.terasology.math.Vector3i;

import javax.vecmath.Tuple3i;

/**
 * Used for entities representing a block in the world
 *
 * @author Immortius <immortius@gmail.com>
 */
public final class BlockComponent implements Component {
    private Vector3i position = new Vector3i();

    // Does this block component exist only for excavation (and should be removed when back at full heath)
    public boolean temporary = false;

    public BlockComponent() {
    }

    public BlockComponent(Tuple3i pos, boolean temporary) {
        this.position.set(pos);
        this.temporary = temporary;
    }

    public Vector3i getPosition() {
        return position;
    }

    public void setPosition(Tuple3i pos) {
        position.set(pos);
    }
}

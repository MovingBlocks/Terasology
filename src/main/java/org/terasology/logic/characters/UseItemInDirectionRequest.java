package org.terasology.logic.characters;

import org.terasology.entitySystem.EntityRef;
import org.terasology.network.ServerEvent;

import javax.vecmath.Vector3f;

/**
 * A request for a player to use an item in a direction
 *
 * @author Immortius
 */
@ServerEvent
public class UseItemInDirectionRequest extends UseItemRequest {

    private Vector3f direction = new Vector3f();

    protected UseItemInDirectionRequest() {
    }

    public UseItemInDirectionRequest(EntityRef usedItem, Vector3f direction) {
        super(usedItem);
        this.direction.set(direction);
    }

    public Vector3f getDirection() {
        return direction;
    }
}

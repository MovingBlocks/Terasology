package org.terasology.logic.characters.events;

import org.terasology.entitySystem.EntityRef;
import org.terasology.network.Replicate;
import org.terasology.network.ServerEvent;

import javax.vecmath.Vector3f;

/**
 * @author Immortius
 */
@ServerEvent
public class AttackInDirectionRequest extends AttackRequest {

    @Replicate
    private Vector3f direction = new Vector3f();

    protected AttackInDirectionRequest() {
    }

    public AttackInDirectionRequest(EntityRef item, Vector3f direction) {
        super(item);
        this.direction.set(direction);
    }

    public Vector3f getDirection() {
        return direction;
    }
}

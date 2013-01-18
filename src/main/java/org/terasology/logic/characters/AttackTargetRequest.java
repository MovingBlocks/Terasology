package org.terasology.logic.characters;

import org.terasology.entitySystem.EntityRef;
import org.terasology.network.ServerEvent;

import javax.vecmath.Vector3f;

/**
 * @author Immortius
 */
@ServerEvent
public class AttackTargetRequest extends AttackRequest {

    private EntityRef target = EntityRef.NULL;
    private Vector3f targetPosition = new Vector3f();

    protected AttackTargetRequest() {
    }

    public AttackTargetRequest(EntityRef item, EntityRef target, Vector3f targetPosition) {
        super(item);
        this.target = target;
        this.targetPosition = targetPosition;
    }

    public EntityRef getTarget() {
        return target;
    }

    public Vector3f getTargetPosition() {
        return targetPosition;
    }
}

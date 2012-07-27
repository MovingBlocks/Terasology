package org.terasology.events;

import org.terasology.components.block.BlockComponent;
import org.terasology.components.world.LocationComponent;
import org.terasology.entitySystem.AbstractEvent;
import org.terasology.entitySystem.EntityRef;

import javax.vecmath.Vector3f;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class ActivateEvent extends AbstractEvent {
    private EntityRef instigator;
    private EntityRef target;
    private Vector3f origin;
    private Vector3f direction;
    private Vector3f hitPosition;
    private Vector3f hitNormal;

    public ActivateEvent(EntityRef instigator, Vector3f origin, Vector3f direction) {
        this(EntityRef.NULL, instigator, origin, direction, origin, new Vector3f());
    }

    public ActivateEvent(EntityRef target, EntityRef instigator) {
        this(target, instigator, new Vector3f(), new Vector3f(), new Vector3f(), new Vector3f());
    }

    public ActivateEvent(EntityRef target, EntityRef instigator, Vector3f origin, Vector3f direction, Vector3f hitPosition, Vector3f hitNormal) {
        this.instigator = instigator;
        this.target = target;
        this.direction = direction;
        this.hitPosition = hitPosition;
        this.hitNormal = hitNormal;
        this.origin = origin;
    }

    public EntityRef getInstigator() {
        return instigator;
    }

    public EntityRef getTarget() {
        return target;
    }

    public Vector3f getOrigin() {
        return origin;
    }

    public Vector3f getDirection() {
        return direction;
    }

    public Vector3f getHitNormal() {
        return hitNormal;
    }

    public Vector3f getTargetLocation() {
        LocationComponent loc = target.getComponent(LocationComponent.class);
        if (loc != null) {
            return loc.getWorldPosition();
        }
        BlockComponent blockComp = target.getComponent(BlockComponent.class);
        if (blockComp != null) {
            return blockComp.getPosition().toVector3f();
        }
        return null;
    }

    public Vector3f getInstigatorLocation() {
        LocationComponent loc = instigator.getComponent(LocationComponent.class);
        if (loc != null) {
            return loc.getWorldPosition();
        }
        return new Vector3f();
    }
}

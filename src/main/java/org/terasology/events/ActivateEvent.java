package org.terasology.events;

import javax.vecmath.Vector3f;

import org.terasology.components.BlockComponent;
import org.terasology.components.LocationComponent;
import org.terasology.entitySystem.AbstractEvent;
import org.terasology.entitySystem.EntityRef;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class ActivateEvent extends AbstractEvent {
    private EntityRef instigator;
    private EntityRef target;
    private Vector3f origin;
    private Vector3f direction;
    private Vector3f normal;

    public ActivateEvent(EntityRef instigator, Vector3f origin, Vector3f direction) {
        this(EntityRef.NULL, instigator, origin, direction, new Vector3f());
    }

    public ActivateEvent(EntityRef target, EntityRef instigator) {
        this(target, instigator, new Vector3f(), new Vector3f(), new Vector3f());
    }

    public ActivateEvent(EntityRef target, EntityRef instigator, Vector3f origin, Vector3f direction, Vector3f normal) {
        this.instigator = instigator;
        this.target = target;
        this.direction = direction;
        this.normal = normal;
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

    public Vector3f getNormal() {
        return normal;
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

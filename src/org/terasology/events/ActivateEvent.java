package org.terasology.events;

import org.terasology.components.BlockComponent;
import org.terasology.components.LocationComponent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.Event;

import javax.vecmath.Vector3f;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class ActivateEvent implements Event {
    private EntityRef instigator;
    private EntityRef target;
    private Vector3f location;
    private Vector3f direction;

    // For OnUser/OnBlock/OnEntity
    public ActivateEvent(EntityRef target, EntityRef instigator) {
        this.instigator = instigator;
        this.target = target;
        LocationComponent loc = target.getComponent(LocationComponent.class);
        if (loc != null) {
            location = loc.getWorldPosition();
        }
        else {
            BlockComponent blockComp = target.getComponent(BlockComponent.class);
            if (blockComp != null) {
                location = blockComp.getPosition().toVector3f();
            }
            else {
                location = new Vector3f();
            }
        }
        direction = new Vector3f();
    }
    
    // For InDirection
    public ActivateEvent(Vector3f location, Vector3f direction, EntityRef instigator) {
        this.instigator = instigator;
        this.target = EntityRef.NULL;
        this.location = location;
        this.direction = direction;
    }

    public EntityRef getInstigator() {
        return instigator;
    }

    public EntityRef getTarget() {
        return target;
    }

    public Vector3f getLocation() {
        return location;
    }

    public Vector3f getDirection() {
        return direction;
    }
}

package org.terasology.events;

import javax.vecmath.Vector3f;

import org.terasology.entitySystem.AbstractEvent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.components.world.LocationComponent;

/**
 * @author aherber <andre.herber@yahoo.de>
 */
public class LocationChangeEvent extends AbstractEvent {
    private EntityRef instigator;
  
    public LocationChangeEvent() {
        instigator = EntityRef.NULL;
    }

    public LocationChangeEvent(EntityRef instigator) {
        this.instigator = instigator;
    }

    public EntityRef getInstigator() {
        return instigator;
    }
    
    public Vector3f getInstigatorLocation() {
        LocationComponent loc = instigator.getComponent(LocationComponent.class);
        if (loc != null) {
            return loc.getWorldPosition();
        }
        return new Vector3f();
    }
}

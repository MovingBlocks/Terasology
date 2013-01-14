package org.terasology.events.crafting;

import org.terasology.entitySystem.AbstractEvent;
import org.terasology.entitySystem.EntityRef;

/**
@author Small-Jeeper
 */
public class CheckRefinementEvent extends AbstractEvent {
    private EntityRef target;
    private EntityRef instigator;


    public CheckRefinementEvent(EntityRef target, EntityRef instigator) {
        this.target     = target;
        this.instigator = instigator;
    }


    public EntityRef getTarget() {
        return target;
    }

    public EntityRef getInstigator(){
        return instigator;
    }

}
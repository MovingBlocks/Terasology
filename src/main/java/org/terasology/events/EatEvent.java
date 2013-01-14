package org.terasology.events;

import org.terasology.entitySystem.AbstractEvent;
import org.terasology.entitySystem.EntityRef;

/**
 * 
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 * 
 */
public class EatEvent extends AbstractEvent {
	private EntityRef instigator;
	private int amount;

	public EatEvent(EntityRef instigator,int amount) {
		this.instigator = instigator;
		this.amount = amount;
	}
	
    public EntityRef getInstigator() {
        return instigator;
    }
	
	public int getAmount() {
		return amount;
	}

}

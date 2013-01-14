package org.terasology.events;

import org.terasology.entitySystem.AbstractEvent;
import org.terasology.entitySystem.EntityRef;

/**
 * 
 * @author Esa-Petri Tirkkonen <esereja@yahoo.co.uk>
 * 
 */
public class ContentmentChangedEvent extends AbstractEvent {
	private EntityRef instigator;
	private int currentContentment;
	private int maxContentment;

	public ContentmentChangedEvent(EntityRef instigator, int currentContentment, int maxContentment) {
		this.instigator = instigator;
		this.currentContentment = currentContentment;
		this.maxContentment = maxContentment;
	}
	
    public EntityRef getInstigator() {
        return instigator;
    }
	
	public int getCurrentContentment() {
		return currentContentment;
	}

	public int getmaxContentment() {
		return maxContentment;
	}
}

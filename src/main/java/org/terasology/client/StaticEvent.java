package org.terasology.client;

import org.terasology.entitySystem.AbstractEvent;
import org.terasology.entitySystem.EntityRef;


public class StaticEvent extends AbstractEvent {
	public void reset() {
		this.cancelled = false;
	}
	public EntityRef getEntity() {
		return null;
	}
}

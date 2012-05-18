package org.terasology.game.client;

import org.terasology.entitySystem.AbstractEvent;
import org.terasology.entitySystem.EntityRef;


public abstract class  ClientEvent extends StaticEvent {
	protected EntityRef target;
	public EntityRef getTarget() {
		return target;
	}

}

package org.terasology.client;

import org.terasology.entitySystem.EntityRef;

public class MouseOverEvent extends MouseEvent {
	EntityRef target;
	public EntityRef getTarget() {
		return target;
	}
}

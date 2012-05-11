package org.terasology.client;

import org.terasology.entitySystem.AbstractEvent;

public class KeyEvent extends AbstractEvent implements ClientEvent {
	protected int key;
	
	public int getKey() {
		return key;
	}
}

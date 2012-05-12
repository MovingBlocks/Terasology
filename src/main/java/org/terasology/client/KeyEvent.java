package org.terasology.client;

import org.terasology.entitySystem.AbstractEvent;

public class KeyEvent extends StaticEvent implements ClientEvent {
	protected int key;
	
	public int getKey() {
		return key;
	}
}

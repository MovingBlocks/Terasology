package org.terasology.game.client;

import org.terasology.entitySystem.AbstractEvent;
import org.terasology.entitySystem.EntityRef;


public class StaticEvent extends AbstractEvent {
	private boolean consumed;
	public void reset() {
		this.cancelled = false;
		this.consumed = false;
	}
	public static StaticEvent makeEvent(String mod, String eventName) {
		StaticEvent event = new StaticEvent() {{}};
		return event;
	}
	public boolean isConsumed() {
		return consumed;
	}
}

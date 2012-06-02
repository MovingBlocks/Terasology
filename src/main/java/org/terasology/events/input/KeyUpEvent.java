package org.terasology.events.input;

import org.terasology.entitySystem.EntityRef;
import org.terasology.game.client.ButtonState;

public class KeyUpEvent extends KeyEvent {

    private static KeyUpEvent event = new KeyUpEvent(0, 0, EntityRef.NULL);

    public static KeyUpEvent create(int key, float delta, EntityRef target) {
        event.reset(delta, target);
        event.setKey(key);
        return event;
    }

    private KeyUpEvent(int key, float delta, EntityRef target) {
        super(key, ButtonState.UP, delta, target);
    }
}

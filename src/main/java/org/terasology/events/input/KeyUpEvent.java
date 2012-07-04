package org.terasology.events.input;

import org.terasology.entitySystem.EntityRef;
import org.terasology.input.ButtonState;

public class KeyUpEvent extends KeyEvent {

    private static KeyUpEvent event = new KeyUpEvent(0, 0);

    public static KeyUpEvent create(int key, float delta) {
        event.reset(delta);
        event.setKey(key);
        return event;
    }

    private KeyUpEvent(int key, float delta) {
        super(key, ButtonState.UP, delta);
    }
}

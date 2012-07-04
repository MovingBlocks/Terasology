package org.terasology.events.input;

import org.terasology.entitySystem.EntityRef;
import org.terasology.input.ButtonState;

public class KeyDownEvent extends KeyEvent {

    private static KeyDownEvent event = new KeyDownEvent(0, 0);

    public static KeyDownEvent create(int key, float delta) {
        event.reset(delta);
        event.setKey(key);
        return event;
    }

    private KeyDownEvent(int key, float delta) {
        super(key, ButtonState.DOWN, delta);
    }

}

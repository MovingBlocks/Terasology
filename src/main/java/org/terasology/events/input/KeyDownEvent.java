package org.terasology.events.input;

import org.terasology.entitySystem.EntityRef;
import org.terasology.game.input.ButtonState;

public class KeyDownEvent extends KeyEvent {

    private static KeyDownEvent event = new KeyDownEvent(0, 0, EntityRef.NULL);

    public static KeyDownEvent create(int key, float delta, EntityRef target) {
        event.reset(delta, target);
        event.setKey(key);
        return event;
    }

    private KeyDownEvent(int key, float delta, EntityRef target) {
        super(key, ButtonState.DOWN, delta, target);
    }

}

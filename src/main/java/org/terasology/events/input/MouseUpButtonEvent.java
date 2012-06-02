package org.terasology.events.input;


import org.terasology.entitySystem.EntityRef;
import org.terasology.game.client.ButtonState;

public class MouseUpButtonEvent extends MouseButtonEvent {

    private static MouseUpButtonEvent event = new MouseUpButtonEvent(0, 0, EntityRef.NULL);

    public static MouseUpButtonEvent create(int button, float delta, EntityRef target) {
        event.reset(delta, target);
        event.setButton(button);
        return event;
    }

    protected MouseUpButtonEvent(int button, float delta, EntityRef target) {
        super(button, ButtonState.UP, delta, target);
    }
}

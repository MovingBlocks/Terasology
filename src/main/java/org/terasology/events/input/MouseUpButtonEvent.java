package org.terasology.events.input;


import org.terasology.entitySystem.EntityRef;
import org.terasology.input.ButtonState;

public class MouseUpButtonEvent extends MouseButtonEvent {

    private static MouseUpButtonEvent event = new MouseUpButtonEvent(0, 0);

    public static MouseUpButtonEvent create(int button, float delta) {
        event.reset(delta);
        event.setButton(button);
        return event;
    }

    protected MouseUpButtonEvent(int button, float delta) {
        super(button, ButtonState.UP, delta);
    }
}

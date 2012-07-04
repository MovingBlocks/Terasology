package org.terasology.events.input;


import org.terasology.entitySystem.EntityRef;
import org.terasology.input.ButtonState;

public class MouseDownButtonEvent extends MouseButtonEvent {

    private static MouseDownButtonEvent event = new MouseDownButtonEvent(0, 0);

    public static MouseDownButtonEvent create(int button, float delta) {
        event.reset(delta);
        event.setButton(button);
        return event;
    }

    protected MouseDownButtonEvent(int button, float delta) {
        super(button, ButtonState.DOWN, delta);
    }

}

package org.terasology.events.input;


import org.terasology.entitySystem.EntityRef;
import org.terasology.input.ButtonState;

public class MouseDownButtonEvent extends MouseButtonEvent {

    private static MouseDownButtonEvent event = new MouseDownButtonEvent(0, 0, EntityRef.NULL);

    public static MouseDownButtonEvent create(int button, float delta, EntityRef target) {
        event.reset(delta, target);
        event.setButton(button);
        return event;
    }

    protected MouseDownButtonEvent(int button, float delta, EntityRef target) {
        super(button, ButtonState.DOWN, delta, target);
    }

}

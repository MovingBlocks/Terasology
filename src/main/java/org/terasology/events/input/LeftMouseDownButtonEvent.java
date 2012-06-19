package org.terasology.events.input;


import org.terasology.entitySystem.EntityRef;

public class LeftMouseDownButtonEvent extends MouseDownButtonEvent {

    private static LeftMouseDownButtonEvent event = new LeftMouseDownButtonEvent(0, EntityRef.NULL);

    public static LeftMouseDownButtonEvent create(float delta, EntityRef target) {
        event.reset(delta, target);
        return event;
    }

    private LeftMouseDownButtonEvent(float delta, EntityRef target) {
        super(0, delta, target);
    }
}
